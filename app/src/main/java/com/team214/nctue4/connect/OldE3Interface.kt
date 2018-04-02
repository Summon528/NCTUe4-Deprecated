package com.team214.nctue4.connect

import android.content.Context
import com.team214.nctue4.model.*

interface OldE3Interface {
    enum class Status {
        SUCCESS, SERVICE_ERROR, WRONG_CREDENTIALS
    }

    fun getLoginTicket(completionHandler: (status: Status,
                                           response: Pair<String, String>?) -> Unit)

    fun getCourseList(completionHandler: (status: Status,
                                          response: ArrayList<CourseItem>?) -> Unit)

    fun getAnnouncementListLogin(count: Int, completionHandler: (status: Status,
                                                                 response: ArrayList<AnnItem>?) -> Unit)

    fun getCourseAnn(courseId: String, courseName: String,
                     completionHandler: (status: Status,
                                         response: ArrayList<AnnItem>?) -> Unit)

    fun getMaterialDocList(courseId: String, context: Context,
                           completionHandler: (status: Status,
                                               response: ArrayList<DocGroupItem>?) -> Unit)

    fun getAttachFileList(documentId: String, courseId: String,
                          completionHandler: (status: Status,
                                              response: ArrayList<AttachItem>?) -> Unit)

    fun getMemberList(courseId :String,
                      completionHandler: (status: Status, response:
                      ArrayList<MemberItem>?) -> Unit)
    fun cancelPendingRequests()
}