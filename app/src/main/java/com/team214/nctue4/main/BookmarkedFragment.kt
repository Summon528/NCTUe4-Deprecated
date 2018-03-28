package com.team214.nctue4.main

import android.content.Intent
import android.graphics.drawable.NinePatchDrawable
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.h6ah4i.android.widget.advrecyclerview.animator.DraggableItemAnimator
import com.h6ah4i.android.widget.advrecyclerview.draggable.RecyclerViewDragDropManager
import com.team214.nctue4.R
import com.team214.nctue4.course.CourseActivity
import com.team214.nctue4.model.CourseDBHelper
import com.team214.nctue4.model.CourseItem
import kotlinx.android.synthetic.main.fragment_old_e3.*
import kotlinx.android.synthetic.main.item_course.view.*
import kotlinx.android.synthetic.main.status_empty.*


class BookmarkedFragment : Fragment() {
    private lateinit var courseDBHelper: CourseDBHelper
    private var courseItems = ArrayList<CourseItem>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        courseDBHelper = CourseDBHelper(context!!)
        if (arguments?.getBoolean("home") == null)
            activity!!.setTitle(R.string.bookmarked_courses)
        return inflater.inflate(R.layout.fragment_old_e3, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        if (arguments?.getBoolean("home") != null)
            old_e3_recycler_view.isNestedScrollingEnabled = false
        courseItems = courseDBHelper.readBookmarkedCourse(
                if (arguments?.getBoolean("home") != null) 5 else null
        )
        updateList()
        super.onViewCreated(view, savedInstanceState)
    }


    private fun updateList() {
        if (courseItems.isEmpty()) empty_request?.visibility = View.VISIBLE
        else {
            val dragDropManager = RecyclerViewDragDropManager()
            val courseAdapter = CourseDragDropAdapter(courseItems,
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
            }, courseDBHelper)
            val wrappedAdapter = dragDropManager.createWrappedAdapter(courseAdapter)
            old_e3_recycler_view?.adapter = wrappedAdapter
            old_e3_recycler_view?.layoutManager = LinearLayoutManager(context)
            old_e3_recycler_view?.addItemDecoration(DividerItemDecoration(context,
                    LinearLayoutManager.VERTICAL))
            dragDropManager.setDraggingItemShadowDrawable(ContextCompat.getDrawable(context!!, R.drawable.ms9_composite_shadow_z6) as NinePatchDrawable)
            dragDropManager.setInitiateOnLongPress(true)
            dragDropManager.setInitiateOnMove(false)
            old_e3_recycler_view.itemAnimator = DraggableItemAnimator()
            dragDropManager.attachRecyclerView(old_e3_recycler_view)

        }
    }
}