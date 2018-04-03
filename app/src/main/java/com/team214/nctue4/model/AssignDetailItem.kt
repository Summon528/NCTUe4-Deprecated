package com.team214.nctue4.model

import java.util.*
import kotlin.collections.ArrayList

class AssignDetailItem(var name: String = "",
                       var content: String = "",
                       var assId: String = "",
                       var startDateTime: Date = Date(),
                       var endDateTime: Date = Date(),
                       var attachItem: ArrayList<AttachItem> = ArrayList(),
                       var sentItem: ArrayList<AttachItem> = ArrayList())