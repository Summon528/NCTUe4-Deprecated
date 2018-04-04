package com.team214.nctue4.model

import android.os.Parcelable
import kotlinx.android.parcel.IgnoredOnParcel
import kotlinx.android.parcel.Parcelize
import java.text.NumberFormat
import java.util.*

@Parcelize
class AttachItem(var name: String,
                 private val _fileSize: String,
                 var url: String) : Parcelable {
    @IgnoredOnParcel
    val fileSize: String = _fileSize
        get() =
            try {
                "${NumberFormat.getNumberInstance(Locale.US).format(field.toInt())} B"
            } catch (e: NumberFormatException) {
                "$field B"
            }
}
