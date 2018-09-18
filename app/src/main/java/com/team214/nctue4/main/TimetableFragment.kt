package com.team214.nctue4.main

import android.graphics.Color
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import com.team214.nctue4.R
import com.team214.nctue4.connect.OldE3Connect
import com.team214.nctue4.connect.OldE3Interface
import com.team214.nctue4.model.CourseDBHelper
import com.team214.nctue4.model.CourseItem
import com.team214.nctue4.model.TimeTableItem
import com.team214.nctue4.utility.DataStatus
import com.team214.nctue4.utility.E3Type
import kotlinx.android.synthetic.main.fragment_timetable.*
import kotlinx.android.synthetic.main.status_error.*
import java.util.*
import kotlin.math.roundToInt


class TimetableFragment : Fragment() {
    private lateinit var oldE3Service: OldE3Connect
    private lateinit var courseDBHelper: CourseDBHelper
    private var courseItems = ArrayList<CourseItem>()
    private lateinit var timeTableItems: Array<ArrayList<TimeTableItem>>
    private var height: Int = 0
    private var minHeight: Int = 0
    private var maxHeight: Int = 0
    private var dataStatus = DataStatus.INIT

    override fun onStop() {
        if (dataStatus == DataStatus.INIT) dataStatus = DataStatus.STOPPED
        if (::oldE3Service.isInitialized) oldE3Service.cancelPendingRequests()
        super.onStop()
    }

    override fun onStart() {
        if (dataStatus == DataStatus.STOPPED) getData()
        super.onStart()
    }
//    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
//        inflater!!.inflate(R.menu.refresh, menu)
//    }
//
//    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
//        return when (item!!.itemId) {
//            R.id.action_refresh -> {
//                if (::oldE3Service.isInitialized) oldE3Service.cancelPendingRequests()
//                getCourseList()
//                true
//            }
//            else -> {
//                super.onOptionsItemSelected(item)
//            }
//        }
//    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        activity!!.setTitle(R.string.timetable)
        setHasOptionsMenu(true)
        courseDBHelper = CourseDBHelper(context!!)
        return inflater.inflate(R.layout.fragment_timetable, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        oldE3Service = (activity as MainActivity).oldE3Service
        courseItems = courseDBHelper.readCourses(E3Type.OLD)
        getData()
        super.onViewCreated(view, savedInstanceState)
    }

    private fun getData() {
        progress_bar?.visibility = View.VISIBLE
        error_request?.visibility = View.GONE
        if (courseItems.isEmpty()) getCourseList()
        else getTimeTable()
    }

    private fun getCourseList() {
        oldE3Service.getCourseList { status, response ->
            activity?.runOnUiThread {
                when (status) {
                    OldE3Interface.Status.SUCCESS -> {
                        courseDBHelper.refreshCourses(response!!, E3Type.OLD)
                        courseItems = response
                        getTimeTable()
                    }
                    else -> {
                        progress_bar?.visibility = View.INVISIBLE
                        error_request?.visibility = View.VISIBLE
                        dataStatus = DataStatus.INIT
                        error_request_retry?.setOnClickListener {
                            getData()
                        }
                    }
                }
            }
        }
    }

    private fun getTimeTable() {

        oldE3Service.getTimeTable(courseItems) { status, response ->
            activity?.runOnUiThread {
                when (status) {
                    OldE3Interface.Status.SUCCESS -> {
                        timeTableItems = response!!
                        updateView()
                        progress_bar?.visibility = View.INVISIBLE
                        course_weekday?.visibility = View.VISIBLE
                        dataStatus = DataStatus.FINISHED
                    }
                    else -> {
                        progress_bar?.visibility = View.INVISIBLE
                        error_request?.visibility = View.VISIBLE
                        dataStatus = DataStatus.INIT
                        error_request_retry?.setOnClickListener {
                            getData()
                        }
                    }
                }
            }
        }
    }

    //  Really Really terrible implementation
    private fun updateView() {
        height = timetable.height / 16
        minHeight = height
        maxHeight = height * 5
        val calendar = Calendar.getInstance()
        var today = calendar.get(Calendar.DAY_OF_WEEK)
        if (calendar.firstDayOfWeek == Calendar.SUNDAY) today--
        else if (today == 7) today = 0

        for (day in 0 until 7) {
            if (day == 0 && timeTableItems[day].size == 0) {
                sun?.visibility = View.GONE
                continue
            }
            if (day == 6 && timeTableItems[day].size == 0) {
                sat?.visibility = View.GONE
                continue
            }
            val linearLayout = LinearLayout(context)
            linearLayout.orientation = LinearLayout.VERTICAL
            linearLayout.layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1.0f)
            (0 until 16).forEach {
                val view = View(context)
                view.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, height)
                view.setBackgroundResource(R.drawable.border)
                if (day == today) view.background.setColorFilter(Color.parseColor("#EEEEEE"), android.graphics.PorterDuff.Mode.ADD)
                linearLayout.addView(view)
            }
            timetable_background.addView(linearLayout)
        }




        for (it in 0 until 7) {
            if ((it == 0 || it == 6) && timeTableItems[it].size == 0) continue
            val relativeLayout = RelativeLayout(context)
            relativeLayout.layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1.0f)
            timeTableItems[it].forEach {
                val textView = TextView(context)
                val params = RelativeLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, height * it.length - 4)
                params.setMargins(2, height * it.section, 2, 2)
                textView.layoutParams = params
                textView.text = "${it.courseName}\n${it.classRoom}"
                textView.setBackgroundResource(R.drawable.timetable_rounded_square)
                textView.background.setColorFilter(it.color, android.graphics.PorterDuff.Mode.MULTIPLY)
                relativeLayout.addView(textView)
            }
            timetable.addView(relativeLayout)
        }

        val courseTime = arrayOf("06:00", "07:00", "08:00", "09:00", "10:10", "11:10", "12:20", "13:20", "14:20", "15:30", "16:30", "17:30", "18:30", "19:30", "20:30", "21:30")
        courseTime.forEach {
            val textView = TextView(context)
            val params = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, height)
            textView.layoutParams = params
            textView.text = it
            course_time_side.addView(textView)
        }
        timetable_root.scaleCall = { resize(it) }
    }

    private fun resize(scaleFactor: Float) {
        height = (height * scaleFactor).roundToInt()
        height = minOf(maxHeight, maxOf(minHeight, height))

        (0 until course_time_side.childCount).map { course_time_side.getChildAt(it) }.forEach {
            it.layoutParams = LinearLayout.LayoutParams(it.layoutParams.width, height)
        }

        (0 until timetable_background.childCount).map { timetable_background.getChildAt(it) as LinearLayout }.forEach {
            val child = it
            (0 until child.childCount).map { child.getChildAt(it) }.forEach {
                it.layoutParams = LinearLayout.LayoutParams(it.layoutParams.width, height)
            }
        }

        val noSun = if (timeTableItems[0].isEmpty()) 1 else 0
        for (i in 0 until timetable.childCount) {
            val child = timetable.getChildAt(i) as RelativeLayout
            for (j in 0 until child.childCount) {
                val params = RelativeLayout.LayoutParams(child.getChildAt(j).layoutParams.width, height * timeTableItems[i + noSun][j].length - 4)
                params.setMargins(2, height * timeTableItems[i + noSun][j].section, 2, 2)
                child.getChildAt(j).layoutParams = params
            }
        }
    }


}