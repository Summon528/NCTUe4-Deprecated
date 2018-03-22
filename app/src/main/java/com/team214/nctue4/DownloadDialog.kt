package com.team214.nctue4


import android.content.DialogInterface
import android.content.Intent
import android.content.res.Resources
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v4.app.Fragment
import android.support.v4.content.FileProvider
import android.support.v7.app.AlertDialog
import android.util.DisplayMetrics
import android.view.*
import android.webkit.MimeTypeMap
import android.widget.EditText
import kotlinx.android.synthetic.main.dialog_download.*
import java.io.File
import kotlin.math.round
import kotlin.math.roundToInt


/**
 * A simple [Fragment] subclass.
 */
class DownloadDialog : DialogFragment() {

    private var onDismissListener: DialogInterface.OnDismissListener? = null

    fun setOnDismissListener(onDismissListener: DialogInterface.OnDismissListener) {
        this.onDismissListener = onDismissListener
    }

    override fun onDismiss(dialog: DialogInterface?) {
        super.onDismiss(dialog)
        if (onDismissListener != null) {
            onDismissListener!!.onDismiss(dialog)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.dialog_download, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dialog.window.requestFeature(Window.FEATURE_NO_TITLE)
        val file = arguments?.getSerializable("file") as File
        download_delete.setOnClickListener {
            val deleteDialog = AlertDialog.Builder(context!!)
            deleteDialog.setTitle(getString(R.string.delete))
                    .setMessage(getString(R.string.confirm_delete, file.name))
                    .setPositiveButton(R.string.positive) { _, _ ->
                        file.delete()
                        dismiss()
                    }
            deleteDialog.show()
        }
        download_rename.setOnClickListener {
            val editDialogBuild = AlertDialog.Builder(context!!)
            editDialogBuild.setTitle(R.string.rename)
            val editText = EditText(context)
            editText.setText(file.name)
            editDialogBuild.setView(editText)
            editText.requestFocus()
            editText.setSelection(0, file.nameWithoutExtension.length)
            editDialogBuild.setPositiveButton(R.string.positive) { _, _ ->
                file.renameTo(File(file.parentFile, editText.text.toString()))
                dismiss()
            }
            val spacing = (20 * Resources.getSystem().displayMetrics.density).roundToInt()
            editDialogBuild.setView(editText, spacing, 0, spacing, 0)
            val editDialog = editDialogBuild.create()
            editDialog.window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)
            editDialog.show()
        }
        download_share.setOnClickListener {
            val intent = Intent(Intent.ACTION_SEND)
            val type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(file.extension)
            val fileUri = FileProvider.getUriForFile(context!!,
                    context!!.applicationContext.packageName + ".com.team214", file)
            intent.setDataAndType(fileUri, type)
            intent.putExtra(Intent.EXTRA_STREAM, fileUri)
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            context!!.startActivity(intent)
            dismiss()
        }
    }

}// Required empty public constructor
