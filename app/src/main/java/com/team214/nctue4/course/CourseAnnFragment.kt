package com.team214.nctue4.course


import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.team214.nctue4.AnnActivity
import com.team214.nctue4.R
import com.team214.nctue4.connect.NewE3Connect
import com.team214.nctue4.connect.NewE3Interface
import com.team214.nctue4.connect.OldE3Connect
import com.team214.nctue4.connect.OldE3Interface
import com.team214.nctue4.model.AnnItem
import com.team214.nctue4.utility.DataStatus
import com.team214.nctue4.utility.E3Type
import kotlinx.android.synthetic.main.fragment_course_ann.*
import kotlinx.android.synthetic.main.status_empty.*
import kotlinx.android.synthetic.main.status_error.*


class CourseAnnFragment : Fragment() {
    private var oldE3Service: OldE3Connect? = null
    private var newE3Service: NewE3Connect? = null
    private var dataStatus = DataStatus.INIT

    override fun onStop() {
        super.onStop()
        oldE3Service?.cancelPendingRequests()
        newE3Service?.cancelPendingRequests()
        if (dataStatus == DataStatus.INIT) dataStatus = DataStatus.STOPPED
    }

    override fun onStart() {
        super.onStart()
        if (dataStatus == DataStatus.STOPPED) getData()
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_course_ann, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getData()
    }

    private fun getData() {
        error_request?.visibility = View.GONE
        progress_bar?.visibility = View.VISIBLE

        val courseId = arguments!!.getString("courseId")
        val courseName = arguments!!.getString("courseName")
        val e3Type = arguments!!.getInt("e3Type")
        activity!!.title = courseName

        if (e3Type == E3Type.NEW) {
            newE3Service = (activity as CourseActivity).newE3Service
            newE3Service!!.getCourseAnn(courseId, courseName) { status, response ->
                activity?.runOnUiThread {
                    when (status) {
                        NewE3Interface.Status.SUCCESS -> {
                            if (response != null) update(response)
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
        } else {
            oldE3Service = (activity as CourseActivity).oldE3Service
            oldE3Service!!.getCourseAnn(courseId, courseName) { status, response ->
                activity?.runOnUiThread {
                    when (status) {
                        OldE3Interface.Status.SUCCESS -> {
                            update(response!!)
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
        }
    }

    private fun update(annItems: ArrayList<AnnItem>) {
        if (annItems.size == 0) {
            empty_request?.visibility = View.VISIBLE
        } else {
            announcement_course_recycler_view?.layoutManager = LinearLayoutManager(context)
            announcement_course_recycler_view?.addItemDecoration(DividerItemDecoration(context,
                    LinearLayoutManager.VERTICAL))
            announcement_course_recycler_view?.adapter = CourseAnnAdapter(annItems) {
                val intent = Intent()
                intent.setClass(activity, AnnActivity::class.java)
                intent.putExtra("annItem", it)
                startActivity(intent)
            }
            announcement_course_recycler_view?.visibility = View.VISIBLE

        }
    }
}
