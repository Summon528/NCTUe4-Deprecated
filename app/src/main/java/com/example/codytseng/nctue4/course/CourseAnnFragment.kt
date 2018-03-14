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


/**
 * A simple [Fragment] subclass.
 */
class CourseAnnFragment : Fragment() {


    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater!!.inflate(R.layout.fragment_course_ann, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d("arguments", arguments.toString())
        val parentActivity = activity as CourseActivity
        val service = parentActivity.service
        val courseId = arguments.getString("courseId")
        service.getCourseAnn(courseId) { status, response ->
            when (status) {
                OldE3Interface.Status.SUCCESS -> {
                    if (course_ann_text_view != null)
                        course_ann_text_view.text = response.toString()
                }
            }
        }
    }
}
