package com.team214.nctue4.main

import android.content.Intent
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.team214.nctue4.R
import com.team214.nctue4.course.CourseActivity
import com.team214.nctue4.model.CourseItem
import com.team214.nctue4.utility.DataStatus
import com.team214.nctue4.utility.OldE3Connect
import com.team214.nctue4.utility.OldE3Interface
import kotlinx.android.synthetic.main.fragment_old_e3.*
import kotlinx.android.synthetic.main.item_course.view.*
import kotlinx.android.synthetic.main.status_empty.*
import kotlinx.android.synthetic.main.status_error.*


class BookmarkedFragment : Fragment() {
    private lateinit var oldE3Service: OldE3Connect
    private var dataStatus = DataStatus.INIT

    override fun onStop() {
        super.onStop()
        if (dataStatus == DataStatus.INIT) dataStatus = DataStatus.STOPPED
        if (dataStatus != DataStatus.FINISHED) oldE3Service.cancelPendingRequests()
    }

    override fun onStart() {
        super.onStart()
        if (dataStatus == DataStatus.STOPPED) getData()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        if (arguments?.getBoolean("home") == null)
            activity!!.setTitle(R.string.bookmarked_courses)
        return inflater.inflate(R.layout.fragment_old_e3, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getData()
    }

    private fun getData() {
        error_request?.visibility = View.GONE
        progress_bar?.visibility = View.VISIBLE

        oldE3Service = (activity as MainActivity).oldE3Service
        oldE3Service.getCourseList { status, response ->
            when (status) {
                OldE3Interface.Status.SUCCESS -> {
                    updateList(response!!)
                }
                else -> {
                    error_request?.visibility = View.VISIBLE
                    dataStatus = DataStatus.INIT
                    error_request_retry?.setOnClickListener { getData() }
                }
            }
            dataStatus = DataStatus.FINISHED
            progress_bar?.visibility = View.GONE
        }
    }

    private fun updateList(courseItems: ArrayList<CourseItem>) {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        val oldE3Bookmarked = HashSet(prefs.getStringSet("oldE3Bookmarked", HashSet<String>()))
        if (oldE3Bookmarked.isEmpty()) empty_request?.visibility = View.VISIBLE
        val bookmarked =
                if (arguments?.getBoolean("home") != null) {
                    val tmp = courseItems.filter { oldE3Bookmarked.contains(it.courseId) }
                    ArrayList(tmp.subList(0, minOf(5, tmp.size)))
                } else ArrayList(courseItems.filter { oldE3Bookmarked.contains(it.courseId) })
        if (bookmarked.isEmpty()) empty_request?.visibility = View.VISIBLE
        else {
            old_e3_recycler_view?.layoutManager = LinearLayoutManager(context)
            if (arguments?.getBoolean("home") != null)
                old_e3_recycler_view?.isNestedScrollingEnabled = false
            old_e3_recycler_view?.addItemDecoration(DividerItemDecoration(context,
                    LinearLayoutManager.VERTICAL))
            old_e3_recycler_view?.adapter = CourseAdapter(bookmarked, HashSet(oldE3Bookmarked),
                    context, fun(view: View, courseId: String) {
                if (oldE3Bookmarked.contains(courseId)) {
                    oldE3Bookmarked.remove(courseId)
                    view.course_star.setColorFilter(ContextCompat.getColor(context!!, R.color.md_grey_500))
                } else {
                    oldE3Bookmarked.add(courseId)
                    view.course_star.setColorFilter(ContextCompat.getColor(context!!, R.color.old_e3))
                }
                prefs.edit().putStringSet("oldE3Bookmarked", oldE3Bookmarked).apply()
            }, {
                val intent = Intent()
                intent.setClass(activity, CourseActivity::class.java)
                intent.putExtra("courseId", it.courseId)
                intent.putExtra("courseName", it.courseName)
                startActivity(intent)
            })
            old_e3_recycler_view?.visibility = View.VISIBLE
        }
        progress_bar?.visibility = View.GONE
    }
}