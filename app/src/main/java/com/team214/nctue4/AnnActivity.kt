package com.team214.nctue4


import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.view.MenuItem
import android.view.View
import android.webkit.WebSettings
import com.team214.nctue4.connect.NewE3WebConnect
import com.team214.nctue4.connect.NewE3WebInterface
import com.team214.nctue4.connect.OldE3Connect
import com.team214.nctue4.model.AnnItem
import com.team214.nctue4.utility.*
import kotlinx.android.synthetic.main.activity_ann.*
import kotlinx.android.synthetic.main.status_error.*
import java.text.SimpleDateFormat
import java.util.*


class AnnActivity : AppCompatActivity() {
    private var oldE3Service: OldE3Connect? = null
    private var newE3Service: NewE3WebConnect? = null
    private var dataStatus = DataStatus.INIT

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
                    downloadFile(fileName, uri, this, this, ann_root, null)
                }
                return
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ann)
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        getData()
    }

    private fun showAnn(annItem: AnnItem) {
        error_request?.visibility = View.GONE
        // replace <img src="/...> to <img src="http://e3.nctu.edu.tw/..."
        val content = annItem!!.content.replace("(?<=(<img[.\\s\\S^>]{0,300}src[ \n]{0,300}=[ \n]{0,300}\"))(/)(?=([^/]))".toRegex(),
                "http://e3.nctu.edu.tw/")
        ann_caption.text = annItem.caption
        ann_courseName.text = annItem.courseName
        val sdf = SimpleDateFormat("yyyy/MM/dd", Locale.US)
        ann_date.text = sdf.format(annItem.beginDate)
        ann_content_web_view.settings.defaultTextEncodingName = "utf-8"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) ann_content_web_view.settings.mixedContentMode = WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE
        ann_content_web_view.loadData(content, "text/html; charset=utf-8", "UTF-8")
        ann_content_web_view.setBackgroundColor(Color.TRANSPARENT)
        announcement_attach.layoutManager = LinearLayoutManager(this)
        announcement_attach.adapter = AnnAttachmentAdapter(this, annItem.attachItems) {
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
        newE3Service = bundle.getParcelable("newE3WebService")
        oldE3Service = bundle.getParcelable("oldE3Service")
        error_request?.visibility = View.GONE
        progress_bar?.visibility = View.VISIBLE
        val annItem = bundle.getParcelable<AnnItem>("annItem")
        if (annItem == null) {
            newE3Service!!.getAnnDetail(bundle.getString("annUrl")) { status, response ->
                when (status) {
                    NewE3WebInterface.Status.SUCCESS -> {
                        this.runOnUiThread {
                            Runnable {
                                showAnn(response!!)
                            }.run()
                        }
                    }
                    else -> {
                        this.runOnUiThread {
                            Runnable {
                                error_request?.visibility = View.VISIBLE
                                dataStatus = DataStatus.INIT
                                error_request_retry?.setOnClickListener { getData() }
                                progress_bar?.visibility = View.GONE
                                dataStatus = DataStatus.FINISHED
                            }.run()
                        }
                    }
                }
            }
        } else {
            showAnn(annItem)
        }
    }
}