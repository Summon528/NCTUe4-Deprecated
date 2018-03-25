package com.team214.nctue4.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
class AttachItem(var name: String, private val _fileSize: String, var url: String) : Parcelable {
    val fileSize: String = _fileSize
        get() = "$field B"
}
