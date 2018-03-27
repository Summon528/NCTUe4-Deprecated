package com.team214.nctue4.main

import android.content.Intent
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.view.*
import com.team214.nctue4.R
import com.team214.nctue4.connect.OldE3Connect
import com.team214.nctue4.connect.OldE3Interface
import com.team214.nctue4.course.CourseActivity
import com.team214.nctue4.model.CourseDBHelper
import com.team214.nctue4.model.CourseItem
import com.team214.nctue4.utility.E3Type
import kotlinx.android.synthetic.main.fragment_old_e3.*
import kotlinx.android.synthetic.main.item_course.view.*
import kotlinx.android.synthetic.main.status_empty.*


class OldE3Fragment : Fragment() {
    private lateinit var oldE3Service: OldE3Connect
    private lateinit var courseDBHelper: CourseDBHelper
    private var courseItems = ArrayList<CourseItem>()

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
                getData()
                true
            }
            else -> {
                super.onOptionsItemSelected(item)
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        activity!!.setTitle(R.string.old_e3)
        setHasOptionsMenu(true)
        courseDBHelper = CourseDBHelper(context!!)
        return inflater.inflate(R.layout.fragment_old_e3, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        courseItems = courseDBHelper.readCourses(E3Type.OLD)
        if (courseItems.isEmpty()) getData()
        else updateList()
        super.onViewCreated(view, savedInstanceState)
    }

    private fun getData() {
        progress_bar.visibility = View.VISIBLE
        oldE3Service = (activity as MainActivity).oldE3Service
        oldE3Service.getCourseList { status, response ->
            when (status) {
                OldE3Interface.Status.SUCCESS -> {
                    courseDBHelper.refreshCourses(response!!, E3Type.OLD)
                    if (old_e3_recycler_view.adapter == null) {
                        courseItems = response
                        updateList()
                    } else {
                        courseItems.clear()
                        courseItems.addAll(courseDBHelper.readCourses(E3Type.OLD))
                        old_e3_recycler_view.adapter.notifyDataSetChanged()
                    }
                    Snackbar.make(old_e3_root, getString(R.string.refresh_success), Snackbar.LENGTH_SHORT).show()
                    progress_bar.visibility = View.INVISIBLE

                }
                else -> {
                    Snackbar.make(old_e3_root, getString(R.string.generic_error), Snackbar.LENGTH_SHORT).show()
                    progress_bar.visibility = View.INVISIBLE
                }
            }
        }
    }


    private fun updateList() {
        if (courseItems.isEmpty()) empty_request?.visibility = View.VISIBLE
        else {
            old_e3_recycler_view?.layoutManager = LinearLayoutManager(context)
            old_e3_recycler_view?.addItemDecoration(DividerItemDecoration(context,
                    LinearLayoutManager.VERTICAL))
            old_e3_recycler_view?.adapter = CourseAdapter(courseItems,
                    context, fun(view: View, course: CourseItem) {
                if (course.bookmarked == 1) {
                    courseDBHelper.bookmarkCourse(course.courseId, 0)
                    view.course_star.setColorFilter(ContextCompat.getColor(context!!, R.color.md_grey_500))
                } else {
                    courseDBHelper.bookmarkCourse(course.courseId, 1)
                    view.course_star.setColorFilter(ContextCompat.getColor(context!!, R.color.old_e3))
                }
            }, {
                val intent = Intent()
                intent.setClass(activity, CourseActivity::class.java)
                intent.putExtra("courseId", it.courseId)
                intent.putExtra("courseName", it.courseName)
                startActivity(intent)
            })

        }
    }
}