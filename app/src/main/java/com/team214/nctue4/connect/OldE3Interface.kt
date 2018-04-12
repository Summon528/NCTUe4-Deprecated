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

    fun getAttachFileList(documentId: String, courseId: String, metaType: Int,
                          completionHandler: (status: Status,
                                              response: ArrayList<AttachItem>?) -> Unit)

    fun getMemberList(courseId: String,
                      completionHandler: (status: Status, response:
                      ArrayList<MemberItem>?) -> Unit)

    fun getScoreData(courseId: String,
                     completionHandler: (status: Status, response: ArrayList<ScoreItem>?) -> Unit)

    fun getAssign(courseId: String,
                  completionHandler: (status: Status, response: ArrayList<AssignItem>?) -> Unit)

    fun getAssignDetail(assId: String, courseId: String, submitId: String,
                        completionHandler: (status: Status, response: AssignItem?) -> Unit)

    fun getTimeTable(courses: ArrayList<CourseItem>,
                     completionHandler: (status: Status, response: Array<ArrayList<TimeTableItem>>?) -> Unit)

    fun cancelPendingRequests()
}