package com.team214.nctue4.connect

import com.team214.nctue4.model.AnnItem
import com.team214.nctue4.model.CourseItem

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

    fun cancelPendingRequests()
}