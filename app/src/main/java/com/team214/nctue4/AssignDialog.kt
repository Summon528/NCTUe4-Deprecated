package com.team214.nctue4


import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v4.content.ContextCompat
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Toast
import com.team214.nctue4.connect.NewE3Connect
import com.team214.nctue4.connect.NewE3WebInterface
import com.team214.nctue4.course.CourseDocDialogAdapter
import com.team214.nctue4.model.AttachItem
import com.team214.nctue4.utility.DataStatus
import com.team214.nctue4.utility.downloadFile
import kotlinx.android.synthetic.main.dialog_assign.*
import kotlinx.android.synthetic.main.status_error.*


class AssignDialog : DialogFragment() {

    private var newE3Service: NewE3Connect? = null

    private var dataStatus = DataStatus.INIT

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.dialog_assign, container, false)
    }

    override fun onStop() {
        super.onStop()
        newE3Service?.cancelPendingRequests()
        if (dataStatus == DataStatus.INIT) dataStatus = DataStatus.STOPPED
    }

    override fun onStart() {
        super.onStart()
        if (dataStatus == DataStatus.STOPPED) getData()
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dialog.window.requestFeature(Window.FEATURE_NO_TITLE)
        getData()
    }

    private lateinit var uri: String
    private lateinit var fileName: String

    private fun getData() {
        newE3Service = arguments!!.getParcelable("newE3Service")
        if (newE3Service == null) {
            updateList(arguments!!.getParcelableArrayList<AttachItem>("submittedFiles"))
        } else {
            newE3Service!!.getAssignSubmission(arguments!!.getString("assignId")) { status, response ->
                activity?.runOnUiThread {
                    when (status) {
                        NewE3WebInterface.Status.SUCCESS -> {
                            updateList(response!!)
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

    private fun updateList(assignFileItems: ArrayList<AttachItem>) {
        if (assignFileItems.isEmpty()) {
            Toast.makeText(context!!, getString(R.string.no_submitted_assign), Toast.LENGTH_SHORT).show()
            dismiss()
        } else {
            course_doc_dialog_recycler_view?.layoutManager = LinearLayoutManager(context)
            course_doc_dialog_recycler_view?.adapter = CourseDocDialogAdapter(context!!, assignFileItems) {
                uri = it.url
                fileName = it.name
                downloadFile(fileName, uri, context!!, activity!!, activity!!.findViewById(R.id.assign_root)) {
                    requestPermissions(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                            0)
                }
                if (ContextCompat.checkSelfPermission(context!!, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        == PackageManager.PERMISSION_GRANTED) {
                    dismiss()
                }

            }
            progress_bar?.visibility = View.GONE
        }
    }


    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            0 -> {
                if ((grantResults.isNotEmpty() &&
                                grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    downloadFile(fileName, uri, context!!, activity!!, activity!!.findViewById(R.id.assign_root), null, null)
                    dismiss()
                }
                return
            }
        }
    }
}

