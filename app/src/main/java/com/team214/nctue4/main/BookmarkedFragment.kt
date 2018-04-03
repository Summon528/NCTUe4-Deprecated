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
import com.team214.nctue4.utility.E3Type
import kotlinx.android.synthetic.main.fragment_couse_list.*
import kotlinx.android.synthetic.main.item_course.view.*
import kotlinx.android.synthetic.main.status_empty.*
import kotlinx.android.synthetic.main.status_empty_compact.*


class BookmarkedFragment : Fragment() {
    private lateinit var courseDBHelper: CourseDBHelper
    private var courseItems = ArrayList<CourseItem>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        courseDBHelper = CourseDBHelper(context!!)
        if (arguments?.getBoolean("home") == null)
            activity!!.setTitle(R.string.bookmarked_courses)
        return inflater.inflate(R.layout.fragment_couse_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        if (arguments?.getBoolean("home") != null)
            course_list_recycler_view.isNestedScrollingEnabled = false
        courseItems = courseDBHelper.readBookmarkedCourse(
                if (arguments?.getBoolean("home") != null) 5 else null
        )
        updateList()
        super.onViewCreated(view, savedInstanceState)
    }


    private fun updateList() {
        if (courseItems.isEmpty())
            (if (arguments?.getBoolean("home") != null) empty_request_compact else empty_request)?.visibility = View.VISIBLE
        else {
            val dragDropManager = RecyclerViewDragDropManager()
            val courseAdapter = CourseDragDropAdapter(courseItems,
                    context, fun(view: View, course: CourseItem) {
                if (course.bookmarked == 1) {
                    courseDBHelper.bookmarkCourse(course.courseId, 0)
                    course.toggleBookmark()
                    view.course_star.setColorFilter(ContextCompat.getColor(context!!, R.color.md_grey_500))
                } else if (course.e3Type == E3Type.OLD) {
                    courseDBHelper.bookmarkCourse(course.courseId, 1)
                    course.toggleBookmark()
                    view.course_star.setColorFilter(ContextCompat.getColor(context!!, R.color.old_e3))
                } else {
                    course.toggleBookmark()
                    view.course_star.setColorFilter(ContextCompat.getColor(context!!, R.color.new_e3))
                }
            }, {
                val intent = Intent()
                intent.setClass(activity, CourseActivity::class.java)
                intent.putExtra("courseId", it.courseId)
                intent.putExtra("courseName", it.courseName)
                intent.putExtra("e3Type", it.e3Type)
                intent.putExtra("oldE3Service", (activity as MainActivity).oldE3Service)
                intent.putExtra("newE3Service", (activity as MainActivity).newE3Service)
                startActivity(intent)
            }, courseDBHelper)
            val wrappedAdapter = dragDropManager.createWrappedAdapter(courseAdapter)
            course_list_recycler_view?.adapter = wrappedAdapter
            course_list_recycler_view?.layoutManager = LinearLayoutManager(context)
            course_list_recycler_view?.addItemDecoration(DividerItemDecoration(context,
                    LinearLayoutManager.VERTICAL))
            dragDropManager.setDraggingItemShadowDrawable(ContextCompat.getDrawable(context!!, R.drawable.ms9_composite_shadow_z6) as NinePatchDrawable)
            dragDropManager.setInitiateOnLongPress(true)
            dragDropManager.setInitiateOnMove(false)
            course_list_recycler_view.itemAnimator = DraggableItemAnimator()
            dragDropManager.attachRecyclerView(course_list_recycler_view)

        }
    }
}