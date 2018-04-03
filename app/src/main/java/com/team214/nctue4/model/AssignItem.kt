package com.team214.nctue4.model

import android.annotation.SuppressLint
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import java.util.*

@Parcelize
@SuppressLint("ParcelCreator")
class AssignItem(var name: String = "", var assignId: String = "",
                 var startDate: Date = Date(), var endDate: Date = Date(),
                 var content: String = "",
                 var attachItem: ArrayList<AttachItem> = ArrayList(),
                 var sentItem: ArrayList<AttachItem> = ArrayList(),
                 val submitId: String = "") : Parcelable