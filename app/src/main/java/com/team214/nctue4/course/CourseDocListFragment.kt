package com.team214.nctue4.course

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.team214.nctue4.R
import com.team214.nctue4.connect.NewE3Connect
import com.team214.nctue4.connect.NewE3Interface
import com.team214.nctue4.connect.OldE3Connect
import com.team214.nctue4.connect.OldE3Interface
import com.team214.nctue4.model.DocGroupItem
import com.team214.nctue4.utility.DataStatus
import com.team214.nctue4.utility.E3Type
import kotlinx.android.synthetic.main.fragment_course_doc.*
import kotlinx.android.synthetic.main.status_empty.*
import kotlinx.android.synthetic.main.status_error.*


class CourseDocListFragment : Fragment() {

    private var oldE3Service: OldE3Connect? = null
    private var newE3Service: NewE3Connect? = null
    private var dataStatus = DataStatus.INIT
    private var e3Type: Int = -1

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

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_course_doc, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getData()
    }

    private fun getData() {
        error_request?.visibility = View.GONE
        progress_bar?.visibility = View.VISIBLE


        val courseId = arguments!!.getString("courseId")
        e3Type = arguments!!.getInt("e3Type")

        if (e3Type == E3Type.OLD) {
            oldE3Service = (activity as CourseActivity).oldE3Service
            oldE3Service!!.getMaterialDocList(courseId, context!!) { status, response ->
                activity?.runOnUiThread {
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
        } else {
            newE3Service = (activity as CourseActivity).newE3Service
            newE3Service!!.getCourseFolder(courseId, context!!) { status, response ->
                activity?.runOnUiThread {
                    when (status) {
                        NewE3Interface.Status.SUCCESS -> {
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
        }
    }

    private fun updateList(docGroupItems: ArrayList<DocGroupItem>) {
        if (docGroupItems.size == 0) {
            empty_request?.visibility = View.VISIBLE
        } else {
            course_doc_list_recycler_view?.layoutManager = LinearLayoutManager(context)
            course_doc_list_recycler_view?.adapter = CourseDocListAdapter(docGroupItems) {
                val dialog = CourseDocDialog()
                val bundle = Bundle()
                bundle.putString("documentId", it.documentId)
                bundle.putString("courseId", it.courseId)
                bundle.putInt("e3Type", e3Type)
                dialog.arguments = bundle
                dialog.show(fragmentManager, "TAG")
            }
            course_doc_list_recycler_view?.visibility = View.VISIBLE
        }
    }
}