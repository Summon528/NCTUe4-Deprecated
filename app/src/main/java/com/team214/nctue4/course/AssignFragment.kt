package com.team214.nctue4.course


import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.team214.nctue4.AssignActivity
import com.team214.nctue4.R
import com.team214.nctue4.connect.NewE3Connect
import com.team214.nctue4.connect.NewE3Interface
import com.team214.nctue4.connect.OldE3Connect
import com.team214.nctue4.connect.OldE3Interface
import com.team214.nctue4.model.AssignItem
import com.team214.nctue4.utility.DataStatus
import com.team214.nctue4.utility.E3Type
import kotlinx.android.synthetic.main.fragment_score.*
import kotlinx.android.synthetic.main.status_empty.*
import kotlinx.android.synthetic.main.status_error.*


class AssignFragment : Fragment() {
    private var oldE3Service: OldE3Connect? = null
    private var newE3Service: NewE3Connect? = null
    private var dataStatus = DataStatus.INIT
    private var e3Type: Int = -1
    private lateinit var assignItems: ArrayList<AssignItem>
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
        return inflater.inflate(R.layout.fragment_assign, container, false)
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
            oldE3Service!!.getAssign(courseId) { status, response ->
                activity?.runOnUiThread {
                    if (status == OldE3Interface.Status.SUCCESS) {
                        assignItems = response!!
                        updateList()
                    } else {
                        error_request?.visibility = View.VISIBLE
                        dataStatus = DataStatus.INIT
                        error_request_retry?.setOnClickListener { getData() }
                    }
                    dataStatus = DataStatus.FINISHED
                    progress_bar?.visibility = View.GONE
                }
            }
        } else {
            newE3Service = (activity as CourseActivity).newE3Service
            newE3Service!!.getAssign(courseId) { status, response ->
                activity?.runOnUiThread {
                    if (status == NewE3Interface.Status.SUCCESS) {
                        assignItems = response!!
                        updateList()
                    } else {
                        error_request?.visibility = View.VISIBLE
                        dataStatus = DataStatus.INIT
                        error_request_retry?.setOnClickListener { getData() }
                    }
                    dataStatus = DataStatus.FINISHED
                    progress_bar?.visibility = View.GONE
                }
            }
        }

    }

    private fun updateList() {
        if (assignItems.isEmpty()) {
            empty_request?.visibility = View.VISIBLE
        } else {
            course_score_recycler_view?.layoutManager = LinearLayoutManager(context)
            course_score_recycler_view?.addItemDecoration(DividerItemDecoration(context,
                    LinearLayoutManager.VERTICAL))
            course_score_recycler_view?.adapter = AssignAdapter(assignItems) {
                val intent = Intent()
                intent.setClass(activity, AssignActivity::class.java)
                intent.putExtra("courseId", arguments!!.getString("courseId"))
                intent.putExtra("courseName", arguments!!.getString("courseName"))
                intent.putExtra("assignItem", it)
                intent.putExtra("newE3Service", newE3Service)
                intent.putExtra("oldE3Service", oldE3Service)
                startActivity(intent)
            }
        }
    }
}
