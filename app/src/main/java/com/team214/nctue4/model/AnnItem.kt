package com.team214.nctue4.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import java.util.*


@Parcelize
class AnnItem(val bulletinId: String,
              val courseName: String,
              val caption: String,
              val content: String,
              val beginDate: Date,
              val endDate: Date,
              var courseId: String,
              val e3Type: Int,
              val attachItems: ArrayList<AttachItem>
) : Parcelable