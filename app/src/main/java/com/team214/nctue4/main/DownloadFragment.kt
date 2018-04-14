package com.team214.nctue4.main


import android.content.DialogInterface
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentActivity
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.team214.nctue4.R
import com.team214.nctue4.utility.openFile
import kotlinx.android.synthetic.main.fragment_download.*
import kotlinx.android.synthetic.main.status_empty.*
import kotlinx.android.synthetic.main.status_empty_compact.*
import java.io.File

class DownloadFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        if (arguments?.getBoolean("home") == null)
            activity!!.setTitle(R.string.download_history)
        return inflater.inflate(R.layout.fragment_download, container, false)
    }


    private var files = ArrayList<File>()
    private lateinit var emptyRequest: View
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        emptyRequest = if (arguments?.getBoolean("home") != null) empty_request_compact else empty_request
        val path = activity!!.getExternalFilesDir(null)
        val dir = File(path, "Download")
        if (dir.exists()) {
            val fileList = dir.listFiles()
            if (fileList.isEmpty()) emptyRequest.visibility = View.VISIBLE
            fileList.sortByDescending { it.lastModified() }
            if (arguments?.getBoolean("home") != null) {
                files.addAll(fileList.slice(0..minOf(4, fileList.size)).filter { it != null })
            } else files.addAll(fileList.filter { it != null })
            download_recycler?.layoutManager = LinearLayoutManager(context)
            if (arguments?.getBoolean("home") != null)
                download_recycler?.isNestedScrollingEnabled = false
            download_recycler?.addItemDecoration(DividerItemDecoration(context,
                    LinearLayoutManager.VERTICAL))
            download_recycler?.adapter = DownloadAdapter(context!!, files,
                    fun(it) {
                        openFile(it.name, it, context!!, activity!!)
                    },
                    fun(it) {
                        val dialog = DownloadDialog()
                        dialog.setOnDismissListener(DialogInterface.OnDismissListener { updateList() })
                        val bundle = Bundle()
                        bundle.putSerializable("file", it)
                        dialog.arguments = bundle
                        dialog.show(fragmentManager, "TAG")

                    })

        } else {
            emptyRequest.visibility = View.VISIBLE
        }
    }

    fun updateList(homeActivity: FragmentActivity? = null) {
        val path = if (homeActivity != null) homeActivity.getExternalFilesDir(null) else
            activity!!.getExternalFilesDir(null)
        val dir = File(path, "Download")
        files.clear()
        val fileList = dir.listFiles()
        if (fileList.isEmpty()) {
            emptyRequest.visibility = View.VISIBLE
            download_recycler?.visibility = View.GONE
        } else {
            fileList.sortByDescending { it.lastModified() }
            if (arguments?.getBoolean("home") != null) {
                files.addAll(fileList.slice(0..minOf(4, fileList.size)).filter { it != null })
            } else files.addAll(fileList)
            download_recycler?.adapter?.notifyDataSetChanged()
        }
    }

}