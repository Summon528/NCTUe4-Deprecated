package com.team214.nctue4.main

import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
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
        getData()
    }

    private fun race() {
        if (oldE3get == AnnGet.FAIL && newE3get == AnnGet.FAIL) {
            error_request?.visibility = View.VISIBLE
            progress_bar?.visibility = View.GONE
            dataStatus = DataStatus.INIT
            error_request_retry?.setOnClickListener { getData() }
            return
        }
        if (oldE3get == AnnGet.FAIL) Toast.makeText(context!!, getString(R.string.old_e3_ann_error), Toast.LENGTH_LONG).show()
        if (newE3get == AnnGet.FAIL) Toast.makeText(context!!, getString(R.string.new_e3_ann_error), Toast.LENGTH_LONG).show()
        if (oldE3get != AnnGet.START && newE3get != AnnGet.START) {
            val annItems = ArrayList<AnnItem>()
            annItems.addAll(newE3AnnItems)
            annItems.addAll(oldE3AnnItems)
            annItems.sortByDescending { it.beginDate }
            updateList(annItems)
            dataStatus = DataStatus.FINISHED
            progress_bar?.visibility = View.GONE
        }
    }

    private fun getData() {
        error_request?.visibility = View.GONE
        error_wrong_credential?.visibility = View.GONE
        progress_bar?.visibility = View.VISIBLE
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
                    else -> {
                        newE3get = AnnGet.FAIL
                        race()
                    }
                }
            }
        }
    }

    private fun updateList(annItems: ArrayList<AnnItem>) {
        if (annItems.size == 0) {
            (if (arguments?.getBoolean("home") != null) empty_request_compact else empty_request)?.visibility = View.VISIBLE
        } else {
            ann_login_recycler_view?.layoutManager = LinearLayoutManager(context)
            ann_login_recycler_view?.addItemDecoration(DividerItemDecoration(context,
                    LinearLayoutManager.VERTICAL))
            val fromHome = arguments?.getBoolean("home") != null
            if (fromHome) ann_login_recycler_view?.isNestedScrollingEnabled = false
            ann_login_recycler_view?.adapter = HomeAnnAdapter(
                    if (fromHome) annItems.slice(0..minOf(4, annItems.size - 1))
                    else annItems.toList(), context!!) {
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
            ann_login_recycler_view?.visibility = View.VISIBLE
        }
        oldE3get = AnnGet.START
        newE3get = AnnGet.START
    }
}

