package com.team214.nctue4.course


import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.view.*
import android.widget.Toast
import com.team214.nctue4.R
import com.team214.nctue4.connect.NewE3Connect
import com.team214.nctue4.connect.NewE3Interface
import com.team214.nctue4.connect.OldE3Connect
import com.team214.nctue4.connect.OldE3Interface
import com.team214.nctue4.model.MemberItem
import com.team214.nctue4.utility.DataStatus
import com.team214.nctue4.utility.E3Type
import kotlinx.android.synthetic.main.fragment_members.*
import kotlinx.android.synthetic.main.item_member.view.*
import kotlinx.android.synthetic.main.status_empty.*
import kotlinx.android.synthetic.main.status_error.*


class MembersFragment : Fragment() {
    private var oldE3Service: OldE3Connect? = null
    private var newE3Service: NewE3Connect? = null
    private var dataStatus = DataStatus.INIT
    private var e3Type: Int = -1
    private lateinit var memberItems: ArrayList<MemberItem>
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
        setHasOptionsMenu(true)
        return inflater.inflate(R.layout.fragment_members, container, false)
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        inflater!!.inflate(R.menu.select_all, menu)
    }

    private var selectAll = false
    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return when (item!!.itemId) {
            R.id.action_select_all -> {
                if (course_member_recycler_view.adapter != null) {
                    if (!selectAll) {
                        multiSelect = true
                        selectCnt = memberItems.size
                        memberItems.forEach { it.selected = true }
                    } else {
                        multiSelect = false
                        selectCnt = 0
                        memberItems.forEach { it.selected = false }
                    }
                    selectAll = !selectAll
                    course_member_recycler_view.adapter.notifyDataSetChanged()
                    setupFab()
                } else {
                    Toast.makeText(context!!, R.string.wait, Toast.LENGTH_SHORT).show()
                }
                true
            }
            else -> {
                super.onOptionsItemSelected(item)
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getData()
    }

    override fun onDestroy() {
        activity!!.findViewById<FloatingActionButton>(R.id.course_fab).visibility = View.GONE
        super.onDestroy()
    }

    private fun getData() {
        error_request?.visibility = View.GONE
        progress_bar?.visibility = View.VISIBLE
        val courseId = arguments!!.getString("courseId")
        e3Type = arguments!!.getInt("e3Type")
        if (e3Type == E3Type.OLD) {
            oldE3Service = (activity as CourseActivity).oldE3Service
            oldE3Service!!.getMemberList(courseId) { status, response ->
                activity?.runOnUiThread {
                    if (status == OldE3Interface.Status.SUCCESS) {
                        memberItems = response!!
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
            newE3Service!!.getMemberList(courseId) { status, response ->
                activity?.runOnUiThread {
                    if (status == NewE3Interface.Status.SUCCESS) {
                        memberItems = response!!
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

    private var selectCnt = 0
    private var multiSelect = false
    private var lstIndex: Int = -1
    private fun updateList() {
        if (memberItems.isEmpty()) {
            empty_request?.visibility = View.VISIBLE
        } else {
            course_member_recycler_view?.layoutManager = LinearLayoutManager(context)
            course_member_recycler_view?.addItemDecoration(DividerItemDecoration(context,
                    LinearLayoutManager.VERTICAL))
            course_member_recycler_view?.adapter = MembersAdapter(context!!,
                    memberItems, fun(view: View, member: MemberItem, position: Int) {
                if (member.selected) selectCnt-- else selectCnt++
                if (!multiSelect && lstIndex != -1 && lstIndex != position) {
                    memberItems[lstIndex].selected = false
                    selectCnt--
                    course_member_recycler_view.adapter.notifyItemChanged(lstIndex)
                }
                lstIndex = position
                if (selectCnt == 0) {
                    multiSelect = false
                    lstIndex = -1
                } else if (selectCnt > 1) multiSelect = true
                member.selected = !member.selected
                view.member_item.setBackgroundColor(
                        if (member.selected)
                            ContextCompat.getColor(context!!, R.color.md_grey_300)
                        else Color.parseColor("#ffffff"))
                setupFab()

            }, fun(view: View, member: MemberItem) {
                multiSelect = true
                if (member.selected) selectCnt-- else selectCnt++
                member.selected = !member.selected
                view.member_item.setBackgroundColor(
                        if (member.selected)
                            ContextCompat.getColor(context!!, R.color.md_grey_300)
                        else Color.parseColor("#ffffff"))
                multiSelect = selectCnt != 0
                setupFab()
            })
        }

    }

    private fun setupFab() {
        val fab = activity!!.findViewById<FloatingActionButton>(R.id.course_fab)
        if (selectCnt > 0) {
            if (fab.visibility != View.VISIBLE) {
                fab.visibility = View.VISIBLE
                fab.setImageDrawable(
                        ContextCompat.getDrawable(context!!, R.drawable.ic_email_black_24dp))
                fab.setOnClickListener {
                    val intent = Intent(Intent.ACTION_SENDTO)
                    intent.type = "text/plain"
                    var emailUri = "mailto: "
                    memberItems.filter { it.selected && it.email != "" }.forEach {
                        emailUri += it.email + ","
                    }
                    emailUri += "?subject=${arguments!!.getString("courseName")}"
                    intent.data = Uri.parse(emailUri)
                    startActivity(intent)
                }
            }
        } else fab.visibility = View.GONE

    }
}
