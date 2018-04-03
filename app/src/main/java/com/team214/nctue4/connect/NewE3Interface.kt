package com.team214.nctue4.connect

import android.content.Context
import com.team214.nctue4.model.*

interface NewE3Interface {
    enum class Status {
        SUCCESS, SERVICE_ERROR, WRONG_CREDENTIALS
    }

    fun getToken(completionHandler: (status: NewE3Interface.Status, response: String?) -> Unit)
    fun getUserId(completionHandler: (status: NewE3Interface.Status, response: String?) -> Unit)
    fun getCourseList(completionHandler: (status: NewE3Interface.Status,
                                          response: ArrayList<CourseItem>?) -> Unit)

    fun getCourseAnn(courseId: String, courseName: String,
                     completionHandler: (status: NewE3Interface.Status,
                                         response: ArrayList<AnnItem>?) -> Unit)

    fun getCourseFolder(courseId: String, context: Context,
                        completionHandler: (status: Status,
                                            response: ArrayList<DocGroupItem>?) -> Unit)

    fun getFiles(courseId: String, folderId: String,
                 completionHandler: (status: Status,
                                     response: ArrayList<AttachItem>?) -> Unit)

    fun getMemberList(courseId: String,
                      completionHandler: (status: NewE3Interface.Status, response:
                      ArrayList<MemberItem>?) -> Unit)

    fun getScoreData(courseId: String,
                     completionHandler: (status: NewE3Interface.Status, response: ArrayList<ScoreItem>?) -> Unit)

    fun getAssign(courseId: String,
                  completionHandler: (status: NewE3Interface.Status, response: ArrayList<AssignItem>?) -> Unit)

    fun getAssignSubmission(assignId: String,
                            completionHandler: (status: Status, response: ArrayList<AttachItem>?) -> Unit)


    fun cancelPendingRequests()
}