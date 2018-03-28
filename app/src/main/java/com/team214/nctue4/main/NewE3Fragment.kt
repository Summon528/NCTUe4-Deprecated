package com.team214.nctue4.main

import android.content.Intent
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.design.widget.Snackbar
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.view.*
import com.team214.nctue4.R
import com.team214.nctue4.connect.NewE3Connect
import com.team214.nctue4.connect.NewE3Interface
import com.team214.nctue4.connect.OldE3Interface
import com.team214.nctue4.course.CourseActivity
import com.team214.nctue4.model.CourseDBHelper
import com.team214.nctue4.model.CourseItem
import com.team214.nctue4.utility.E3Type
import kotlinx.android.synthetic.main.fragment_old_e3.*
import kotlinx.android.synthetic.main.item_course.view.*
import kotlinx.android.synthetic.main.status_empty.*
import kotlinx.android.synthetic.main.status_error.*


class NewE3Fragment : Fragment() {
    private lateinit var newE3Service: NewE3Connect
    private lateinit var courseDBHelper: CourseDBHelper
    private var courseItems = ArrayList<CourseItem>()

    override fun onStop() {
        if (::newE3Service.isInitialized) newE3Service.cancelPendingRequests()
        super.onStop()
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        inflater!!.inflate(R.menu.refresh, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return when (item!!.itemId) {
            R.id.action_refresh -> {
                if (::newE3Service.isInitialized) newE3Service.cancelPendingRequests()
                getData()
                true
            }
            else -> {
                super.onOptionsItemSelected(item)
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        activity!!.setTitle(R.string.new_e3)
        setHasOptionsMenu(true)
        courseDBHelper = CourseDBHelper(context!!)
        return inflater.inflate(R.layout.fragment_old_e3, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        courseItems = courseDBHelper.readCourses(E3Type.NEW)
        if (courseItems.isEmpty()) getData()
        else updateList()
        super.onViewCreated(view, savedInstanceState)
    }

    private fun getData() {
        error_request?.visibility = View.GONE
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        val newE3UserId = prefs.getString("newE3UserId", "")
        val newE3Token = prefs.getString("newE3Token", "")
        val studentId = prefs.getString("studentId", "")
        val studentPortalPassword = prefs.getString("studentPortalPassword", "")
        progress_bar.visibility = View.VISIBLE
        newE3Service = NewE3Connect(studentId, studentPortalPassword, newE3UserId, newE3Token)
        if (newE3UserId == "" || newE3Token == "") {
            newE3Service.getToken { status: NewE3Interface.Status, response: String? ->
                if (status == NewE3Interface.Status.SUCCESS) {
                    prefs.edit().putString("newE3Token", response).apply()
                    newE3Service.getUserId { status2: NewE3Interface.Status, response2: String? ->
                        if (status2 == NewE3Interface.Status.SUCCESS) {
                            prefs.edit().putString("newE3UserId", response2).apply()
                            getData2()
                        }
                        else{
                            error_request?.visibility = View.VISIBLE
                            progress_bar?.visibility = View.GONE
                            error_request_retry?.setOnClickListener { getData() }
                        }
                    }
                } else {
                    error_request?.visibility = View.VISIBLE
                    progress_bar?.visibility = View.GONE
                    error_request_retry?.setOnClickListener { getData() }
                }
            }
        } else getData2()
    }

    private fun getData2() {
        newE3Service.getCourseList { status, response ->
            when (status) {
                OldE3Interface.Status.SUCCESS -> {
                    courseDBHelper.refreshCourses(response!!, E3Type.NEW)
                    if (old_e3_recycler_view.adapter == null) {
                        courseItems = response
                        updateList()
                    } else {
                        courseItems.clear()
                        courseItems.addAll(courseDBHelper.readCourses(E3Type.NEW))
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