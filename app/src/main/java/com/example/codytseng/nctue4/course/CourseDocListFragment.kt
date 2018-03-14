package com.example.codytseng.nctue4.course

import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.codytseng.nctue4.R
import com.example.codytseng.nctue4.utility.OldE3Interface
import kotlinx.android.synthetic.main.fragment_course_ann.*
import kotlinx.android.synthetic.main.fragment_course_doc_handout.*

/**
 * Created by CodyTseng on 3/14/2018.
 */
class CourseDocListFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater!!.inflate(R.layout.fragment_course_doc_handout, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        val docType = arguments.getString("docType")
        Log.d("123",docType)
        val parentActivity = activity as CourseActivity
        val service = parentActivity.service
        val courseId = arguments.getString("courseId")
        service.getMaterialDocList(courseId, docType) { status, response ->
            when (status) {
                OldE3Interface.Status.SUCCESS -> {
                    if (course_doc_list_text_view != null)
                        course_doc_list_text_view.text = response.toString()
                }
            }
        }
        super.onViewCreated(view, savedInstanceState)
    }
}