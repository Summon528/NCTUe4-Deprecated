package com.team214.nctue4.course


import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.webkit.WebSettings
import android.widget.Toast
import com.google.firebase.analytics.FirebaseAnalytics
import com.team214.nctue4.AnnAttachmentAdapter
import com.team214.nctue4.R
import com.team214.nctue4.connect.NewE3Connect
import com.team214.nctue4.connect.OldE3Connect
import com.team214.nctue4.connect.OldE3Interface
import com.team214.nctue4.model.AssignItem
import com.team214.nctue4.model.AttachItem
import com.team214.nctue4.utility.DataStatus
import com.team214.nctue4.utility.downloadFile
import kotlinx.android.synthetic.main.activity_assign.*
import kotlinx.android.synthetic.main.status_error.*
import java.text.SimpleDateFormat
import java.util.*


class AssignActivity : AppCompatActivity() {
    private var mFirebaseAnalytics: FirebaseAnalytics? = null
    private var oldE3Service: OldE3Connect? = null
    private var newE3Service: NewE3Connect? = null
    private var dataStatus = DataStatus.INIT
    private var submittedFiles: ArrayList<AttachItem>? = null
    private var assignId = ""

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
            R.id.action_download_assign -> {
                if (oldE3Service != null && submittedFiles == null) {
                    Toast.makeText(this, getString(R.string.wait), Toast.LENGTH_SHORT).show()
                } else {
                    val dialog = AssignDialog()
                    val bundle = Bundle()
                    bundle.putParcelableArrayList("submittedFiles", submittedFiles)
                    bundle.putParcelable("newE3Service", newE3Service)
                    bundle.putString("assignId", assignId)
                    dialog.arguments = bundle
                    dialog.show(supportFragmentManager, "TAG")
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this)
        setContentView(R.layout.activity_assign)
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        getData()
    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.download_asign, menu)
        return true
    }

    private fun showAssign(assignItem: AssignItem) {
        error_request?.visibility = View.GONE
        ann_caption.text = assignItem.name
        val sdf = SimpleDateFormat("yyyy/MM/dd hh:mm", Locale.TAIWAN)
        assign_start_date.text = sdf.format(assignItem.startDate)
        assign_end_date.text = sdf.format(assignItem.endDate)
        ann_content_web_view.settings.defaultTextEncodingName = "utf-8"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) ann_content_web_view.settings.mixedContentMode = WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE
        ann_content_web_view.loadData(assignItem.content, "text/html; charset=utf-8", "UTF-8")
        ann_content_web_view.setBackgroundColor(Color.TRANSPARENT)
        assign_attach.layoutManager = LinearLayoutManager(this)
        assign_attach.adapter = AnnAttachmentAdapter(this, assignItem.attachItem) {
            uri = it.url
            fileName = it.name
            downloadFile(fileName, uri, this, this, assign_root)

        }
        ann_container?.visibility = View.VISIBLE
        progress_bar?.visibility = View.GONE
        dataStatus = DataStatus.FINISHED
    }

    private fun getData() {
        val bundle = intent.extras
        toolbar.title = bundle.getString("courseName")
        val courseId = bundle.getString("courseId")
        val assignItem = bundle.getParcelable<AssignItem>("assignItem")
        assignId = assignItem.assignId
        newE3Service = bundle.getParcelable("newE3Service")
        oldE3Service = bundle.getParcelable("oldE3Service")
        error_request?.visibility = View.GONE
        progress_bar?.visibility = View.VISIBLE
        if (newE3Service != null) {
            showAssign(assignItem)
        } else {
            oldE3Service!!.getAssignDetail(assignItem.assignId, courseId, assignItem.submitId) { status, response ->
                this.runOnUiThread {
                    when (status) {
                        OldE3Interface.Status.SUCCESS -> {
                            submittedFiles = response!!.sentItem
                            showAssign(response)
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