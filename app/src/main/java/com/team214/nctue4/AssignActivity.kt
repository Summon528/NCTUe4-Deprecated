package com.team214.nctue4


import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.webkit.WebSettings
import com.team214.nctue4.connect.NewE3Connect
import com.team214.nctue4.connect.NewE3WebInterface
import com.team214.nctue4.connect.OldE3Connect
import com.team214.nctue4.connect.OldE3Interface
import com.team214.nctue4.model.AssignItem
import com.team214.nctue4.utility.DataStatus
import com.team214.nctue4.utility.downloadFile
import kotlinx.android.synthetic.main.activity_assign.*
import kotlinx.android.synthetic.main.status_error.*
import java.text.SimpleDateFormat
import java.util.*


class AssignActivity : AppCompatActivity() {
    private var oldE3Service: OldE3Connect? = null
    private var newE3Service: NewE3Connect? = null
    private var dataStatus = DataStatus.INIT
    private var courseId: String? = null
    private var courseName: String? = null
    private var e3Type: Int? = null

    override fun onStop() {
        super.onStop()
        if (dataStatus != DataStatus.FINISHED) {
            oldE3Service?.cancelPendingRequests()
            newE3Service?.cancelPendingRequests()
        }
        if (dataStatus == DataStatus.INIT) dataStatus = DataStatus.STOPPED
    }

    override fun onStart() {
        super.onStart()
        if (dataStatus == DataStatus.STOPPED) getData()
    }

    private lateinit var uri: String
    private lateinit var fileName: String

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            0 -> {
                if ((grantResults.isNotEmpty() &&
                                grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    downloadFile(fileName, uri, this, this, ann_root, null, null)
                }
                return
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_assign)
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        getData()
    }


    private fun showAnn(assignItem: AssignItem) {
        error_request?.visibility = View.GONE
        ann_caption.text = assignItem.name
//        ann_courseName.text = assignItem.courseName
        val sdf = SimpleDateFormat("yyyy/MM/dd hh:mm:ss", Locale.TAIWAN)
        ann_date.text = sdf.format(assignItem.startDate) + sdf.format(assignItem.endDate)
        ann_content_web_view.settings.defaultTextEncodingName = "utf-8"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) ann_content_web_view.settings.mixedContentMode = WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE
        Log.d("assignItem", assignItem.content)
        ann_content_web_view.loadData(assignItem.content, "text/html; charset=utf-8", "UTF-8")
        ann_content_web_view.setBackgroundColor(Color.TRANSPARENT)
        assign_attach.layoutManager = LinearLayoutManager(this)
        assign_attach.adapter = AnnAttachmentAdapter(this, assignItem.attachItem) {
            uri = it.url
            fileName = it.name
            downloadFile(fileName, uri, this, this, ann_root) {
                requestPermissions(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                        0)
            }

        }
        assign_submit.layoutManager = LinearLayoutManager(this)
        assign_submit.adapter = AnnAttachmentAdapter(this, assignItem.sentItem) {
            uri = it.url
            fileName = it.name
            downloadFile(fileName, uri, this, this, ann_root) {
                requestPermissions(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                        0)
            }

        }
        ann_container?.visibility = View.VISIBLE
        progress_bar?.visibility = View.GONE
        dataStatus = DataStatus.FINISHED
    }

    private fun getData() {
        val bundle = intent.extras
        val courseId = bundle.getString("courseId")
        val assignItem = bundle.getParcelable<AssignItem>("assignItem")
        newE3Service = bundle.getParcelable("newE3Service")
        oldE3Service = bundle.getParcelable("oldE3Service")
        error_request?.visibility = View.GONE
        progress_bar?.visibility = View.VISIBLE
        if (newE3Service != null) {
            newE3Service!!.getAssignSubmission(assignItem.assignId) { status, response ->
                this.runOnUiThread {
                    when (status) {
                        NewE3WebInterface.Status.SUCCESS -> {
                            assignItem.sentItem = response!!
                            showAnn(assignItem)
                        }
                        else -> {
                            error_request?.visibility = View.VISIBLE
                            dataStatus = DataStatus.INIT
                            error_request_retry?.setOnClickListener { getData() }
                            progress_bar?.visibility = View.GONE
                            dataStatus = DataStatus.FINISHED
                        }
                    }
                }
            }

        } else {
            oldE3Service!!.getAssignDetail(assignItem.assignId, courseId, assignItem.submitId) { status, response ->
                this.runOnUiThread {
                    when (status) {
                        OldE3Interface.Status.SUCCESS -> {
                            Log.d("QQQQ", response!!.sentItem.toString())
                            showAnn(response!!)
                        }
                        else -> {
                            error_request?.visibility = View.VISIBLE
                            dataStatus = DataStatus.INIT
                            error_request_retry?.setOnClickListener { getData() }
                            progress_bar?.visibility = View.GONE
                            dataStatus = DataStatus.FINISHED

                        }
                    }
                }
            }
        }
    }
}