package com.team214.nctue4.main

import android.graphics.Color
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.app.Fragment
import android.view.*
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import com.team214.nctue4.R
import com.team214.nctue4.connect.OldE3Connect
import com.team214.nctue4.connect.OldE3Interface
import com.team214.nctue4.model.CourseDBHelper
import com.team214.nctue4.model.CourseItem
import com.team214.nctue4.model.TimeTableItem
import com.team214.nctue4.utility.E3Type
import kotlinx.android.synthetic.main.fragment_timetable.*
import java.util.*


class TimetableFragment : Fragment() {
    private lateinit var oldE3Service: OldE3Connect
    private lateinit var courseDBHelper: CourseDBHelper
    private var courseItems = ArrayList<CourseItem>()
    private lateinit var timeTableItems: Array<ArrayList<TimeTableItem>>

    override fun onStop() {
        if (::oldE3Service.isInitialized) oldE3Service.cancelPendingRequests()
        super.onStop()
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        inflater!!.inflate(R.menu.refresh, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return when (item!!.itemId) {
            R.id.action_refresh -> {
                if (::oldE3Service.isInitialized) oldE3Service.cancelPendingRequests()
                getCourseList()
                true
            }
            else -> {
                super.onOptionsItemSelected(item)
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        activity!!.setTitle(R.string.timetable)
        setHasOptionsMenu(true)
        courseDBHelper = CourseDBHelper(context!!)
        return inflater.inflate(R.layout.fragment_timetable, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        oldE3Service = (activity as MainActivity).oldE3Service
        courseItems = courseDBHelper.readCourses(E3Type.OLD)
        if (courseItems.isEmpty()) getCourseList()
        else getTimeTable()
        super.onViewCreated(view, savedInstanceState)
    }

    private fun getCourseList() {
        progress_bar.visibility = View.VISIBLE
        oldE3Service.getCourseList { status, response ->
            activity?.runOnUiThread {
                when (status) {
                    OldE3Interface.Status.SUCCESS -> {
                        courseDBHelper.refreshCourses(response!!, E3Type.OLD)
                        getTimeTable()
                    }
                    else -> {
                        Snackbar.make(timetable_root, getString(R.string.generic_error), Snackbar.LENGTH_SHORT).show()
                        progress_bar.visibility = View.INVISIBLE
                    }
                }
            }
        }
    }

    private fun getTimeTable() {
        progress_bar.visibility = View.VISIBLE
        oldE3Service.getTimeTable(courseItems) { status, response ->
            activity?.runOnUiThread {
                when (status) {
                    OldE3Interface.Status.SUCCESS -> {
                        timeTableItems = response!!
                        updateView()
                        progress_bar.visibility = View.INVISIBLE
                    }
                    else -> {
                        Snackbar.make(timetable_root, getString(R.string.generic_error), Snackbar.LENGTH_SHORT).show()
                        progress_bar.visibility = View.INVISIBLE
                    }
                }
            }
        }
    }

    private fun updateView() {
        val height = timetable_root.height / 17
        val calendar = Calendar.getInstance()
        var today = calendar.get(Calendar.DAY_OF_WEEK)
        if (calendar.firstDayOfWeek == Calendar.SUNDAY) today--
        else if (today == 7) today = 0
        val weekdayString = arrayOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
        for (day in 0 until 7) {
            if ((day == 0 || day == 6) && timeTableItems[day].size == 0) continue
            val linearLayout = LinearLayout(context)
            linearLayout.orientation = LinearLayout.VERTICAL
            linearLayout.layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1.0f)
            (0 until 17).forEach {
                if (it == 0) {
                    val view = TextView(context)
                    val params = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, height)
                    params.gravity = Gravity.CENTER
                    view.layoutParams = params
                    view.text = weekdayString[day]
                    linearLayout.addView(view)
                } else {
                    val view = View(context)
                    view.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, height)
                    view.setBackgroundResource(R.drawable.border)
                    if (day == today) view.background.setColorFilter(Color.parseColor("#EEEEEE"), android.graphics.PorterDuff.Mode.ADD)
                    linearLayout.addView(view)
                }
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
                params.setMargins(2, height * (it.section + 1), 2, 2)
                textView.layoutParams = params
                textView.text = "${it.courseName}\n${it.classRoom}"
                textView.setBackgroundResource(R.drawable.ann_olde3_rounded_squre)
                textView.background.setColorFilter(it.color, android.graphics.PorterDuff.Mode.MULTIPLY)
                relativeLayout.addView(textView)
            }
            timetable.addView(relativeLayout)
        }
    }


}