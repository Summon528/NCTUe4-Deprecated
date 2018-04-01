package com.team214.nctue4.course


import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.team214.nctue4.R
import com.team214.nctue4.connect.NewE3Connect
import com.team214.nctue4.connect.NewE3Interface
import com.team214.nctue4.connect.OldE3Connect
import com.team214.nctue4.connect.OldE3Interface
import com.team214.nctue4.model.MemberItem
import com.team214.nctue4.utility.DataStatus
import com.team214.nctue4.utility.E3Type
import kotlinx.android.synthetic.main.fragment_members.*
import kotlinx.android.synthetic.main.status_empty.*
import kotlinx.android.synthetic.main.status_error.*


class MembersFragment : Fragment() {
    private var oldE3Service: OldE3Connect? = null
    private var newE3Service: NewE3Connect? = null
    private var dataStatus = DataStatus.INIT
    private var e3Type: Int = -1

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_members, container, false)
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
            oldE3Service!!.getMemberList(courseId) { status, response ->
                if (status == OldE3Interface.Status.SUCCESS) {
                    updateList(response!!)
                } else {
                    error_request?.visibility = View.VISIBLE
                    dataStatus = DataStatus.INIT
                    error_request_retry?.setOnClickListener { getData() }
                }
                dataStatus = DataStatus.FINISHED
                progress_bar?.visibility = View.GONE
            }
        } else {
            newE3Service = (activity as CourseActivity).newE3Service
            newE3Service!!.getMemberList(courseId) { status, response ->
                activity?.runOnUiThread {
                    if (status == NewE3Interface.Status.SUCCESS) {
                        updateList(response!!)
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

    private fun updateList(data: Array<ArrayList<MemberItem>>) {
        if (data[0].isEmpty() && data[1].isEmpty() && data[2].isEmpty()) {
            empty_request?.visibility = View.VISIBLE
        } else {
            course_member_recycler_view?.layoutManager = LinearLayoutManager(context)
            course_member_recycler_view?.addItemDecoration(DividerItemDecoration(context,
                    LinearLayoutManager.VERTICAL))
            course_member_recycler_view?.adapter = MembersAdapter(context!!, ArrayList(data[0] + data[1] + data[2])) {}
        }
    }

}
