package com.team214.nctue4.main

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.team214.nctue4.AnnActivity
import com.team214.nctue4.LoginActivity
import com.team214.nctue4.R
import com.team214.nctue4.connect.NewE3WebConnect
import com.team214.nctue4.connect.NewE3WebInterface
import com.team214.nctue4.connect.OldE3Connect
import com.team214.nctue4.connect.OldE3Interface
import com.team214.nctue4.model.AnnItem
import com.team214.nctue4.utility.AnnGet
import com.team214.nctue4.utility.DataStatus
import com.team214.nctue4.utility.E3Type
import kotlinx.android.synthetic.main.fragment_ann.*
import kotlinx.android.synthetic.main.status_empty.*
import kotlinx.android.synthetic.main.status_empty_compact.*
import kotlinx.android.synthetic.main.status_error.*
import kotlinx.android.synthetic.main.status_wrong_credential.*


class HomeAnnFragment : Fragment() {
    private lateinit var oldE3Service: OldE3Connect
    private lateinit var newE3WebService: NewE3WebConnect
    private var dataStatus = DataStatus.INIT
    private var oldE3get = AnnGet.START
    private var newE3get = AnnGet.START
    private var oldE3AnnItems = ArrayList<AnnItem>()
    private var newE3AnnItems = ArrayList<AnnItem>()
    private var recyclerView: RecyclerView? = null
    private val annItems = ArrayList<AnnItem>()

    override fun onStop() {
        super.onStop()
        if (dataStatus == DataStatus.INIT) dataStatus = DataStatus.STOPPED
        oldE3Service.cancelPendingRequests()
        newE3WebService.cancelPendingRequests()
    }

    override fun onStart() {
        super.onStart()
        if (dataStatus == DataStatus.STOPPED) getData()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        if (arguments?.getBoolean("home") == null)
            activity!!.setTitle(R.string.title_ann)
        return inflater.inflate(R.layout.fragment_ann, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        ann_swipe_refresh_layout.isEnabled = arguments?.getBoolean("home") == null
        recyclerView = RecyclerView(context!!)
        recyclerView?.layoutParams = RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        if (ann_swipe_refresh_layout.isEnabled) {
            ann_swipe_refresh_layout.visibility = View.VISIBLE
            ann_swipe_refresh_layout.addView(recyclerView)
            ann_swipe_refresh_layout.setOnRefreshListener {
                ann_swipe_refresh_layout.isRefreshing = true
                getData()
            }
        } else {
            ann_root.addView(recyclerView)
        }
        progress_bar?.visibility = View.VISIBLE
        getData()
    }

    private fun race() {
        if (recyclerView == null || context == null) return
        if (newE3get == AnnGet.NEW_E3_NOT_INIT && oldE3get != AnnGet.START && !(context as Activity).isFinishing)
            Toast.makeText(context, getString(R.string.new_e3_not_init), Toast.LENGTH_LONG).show()
        if (oldE3get == AnnGet.FAIL && (newE3get == AnnGet.FAIL || newE3get == AnnGet.NEW_E3_NOT_INIT)) {
            error_request?.visibility = View.VISIBLE
            progress_bar?.visibility = View.GONE
            dataStatus = DataStatus.INIT
            error_request_retry?.setOnClickListener {
                progress_bar?.visibility = View.VISIBLE
                getData()
            }
            return
        }
        if (oldE3get == AnnGet.FAIL && newE3get == AnnGet.SUCCESS && !(context as Activity).isFinishing)
            Toast.makeText(context, getString(R.string.old_e3_ann_error), Toast.LENGTH_LONG).show()
        if (newE3get == AnnGet.FAIL && oldE3get == AnnGet.SUCCESS && !(context as Activity).isFinishing)
            Toast.makeText(context, getString(R.string.new_e3_ann_error), Toast.LENGTH_LONG).show()
        if (oldE3get != AnnGet.START && newE3get != AnnGet.START) {
            annItems.clear()
            annItems.addAll(newE3AnnItems)
            annItems.addAll(oldE3AnnItems)
            annItems.sortByDescending { it.beginDate }
            updateList()
            dataStatus = DataStatus.FINISHED
            progress_bar?.visibility = View.GONE
        }
    }

    private fun getData() {
        error_request?.visibility = View.GONE
        error_wrong_credential?.visibility = View.GONE
        oldE3Service = (activity as MainActivity).oldE3Service
        oldE3Service.getAnnouncementListLogin(
                if (arguments?.getBoolean("home") != null) 5 else 100) { status, response ->
            activity?.runOnUiThread {
                when (status) {
                    OldE3Interface.Status.SUCCESS -> {
                        oldE3AnnItems = response!!
                        oldE3get = AnnGet.SUCCESS
                        race()
                    }
                    OldE3Interface.Status.WRONG_CREDENTIALS -> {
                        error_wrong_credential?.visibility = View.VISIBLE
                        progress_bar?.visibility = View.GONE
                        dataStatus = DataStatus.FINISHED
                        login_again_button?.setOnClickListener {
                            val intent = Intent()
                            intent.setClass(context, LoginActivity::class.java)
                            intent.putExtra("reLogin", true)
                            startActivity(intent)
                            activity!!.finish()
                        }
                    }
                    else -> {
                        oldE3get = AnnGet.FAIL
                        race()
                    }
                }
            }
        }

        newE3WebService = (activity as MainActivity).newE3WebService

        newE3WebService.getAnn { status, response ->
            activity?.runOnUiThread {
                when (status) {
                    NewE3WebInterface.Status.SUCCESS -> {
                        newE3AnnItems = response!!
                        newE3get = AnnGet.SUCCESS
                        race()
                    }
                    NewE3WebInterface.Status.WRONG_CREDENTIALS -> {
                        error_wrong_credential?.visibility = View.VISIBLE
                        progress_bar?.visibility = View.GONE
                        dataStatus = DataStatus.FINISHED
                        login_again_button?.setOnClickListener {
                            val intent = Intent()
                            intent.setClass(context, LoginActivity::class.java)
                            intent.putExtra("reLogin", true)
                            startActivity(intent)
                            activity!!.finish()
                        }
                    }
                    NewE3WebInterface.Status.NOT_INIT -> {
                        newE3get = AnnGet.NEW_E3_NOT_INIT
                        race()
                    }
                    else -> {
                        newE3get = AnnGet.FAIL
                        race()
                    }
                }
            }
        }
    }

    private fun updateList() {
        if (annItems.size == 0) {
            recyclerView?.visibility = View.GONE
            (if (arguments?.getBoolean("home") != null) empty_request_compact else empty_request)?.visibility = View.VISIBLE
        } else {
            (if (arguments?.getBoolean("home") != null) empty_request_compact else empty_request)?.visibility = View.GONE
            if (recyclerView?.adapter != null) recyclerView!!.adapter.notifyDataSetChanged()
            else {
                recyclerView?.layoutManager = LinearLayoutManager(context)
                recyclerView?.addItemDecoration(DividerItemDecoration(context,
                        LinearLayoutManager.VERTICAL))
                val fromHome = arguments?.getBoolean("home") != null
                if (fromHome) recyclerView?.isNestedScrollingEnabled = false
                recyclerView?.adapter = HomeAnnAdapter(
                        if (fromHome) ArrayList(annItems.slice(0..minOf(4, annItems.size - 1)))
                        else annItems, context!!) {
                    val intent = Intent()
                    intent.setClass(activity, AnnActivity::class.java)
                    intent.putExtra("fromHome", true)
                    if (it.e3Type == E3Type.OLD) {
                        intent.putExtra("annItem", it)
                        intent.putExtra("oldE3Service", oldE3Service)
                    } else {
                        intent.putExtra("newE3WebService", newE3WebService)
                        intent.putExtra("newE3Service", (activity as MainActivity).newE3Service)
                        intent.putExtra("annUrl", it.bulletinId)
                    }
                    startActivity(intent)
                }
                recyclerView?.visibility = View.VISIBLE
            }
        }
        oldE3get = AnnGet.START
        newE3get = AnnGet.START
        ann_swipe_refresh_layout?.isRefreshing = false
    }
}

