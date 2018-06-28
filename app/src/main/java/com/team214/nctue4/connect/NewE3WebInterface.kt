package com.team214.nctue4.connect

import com.team214.nctue4.model.AnnItem

interface NewE3WebInterface {
    enum class Status {
        SUCCESS, SERVICE_ERROR, WRONG_CREDENTIALS, NOT_INIT
    }

    fun getCookie(completionHandler: (status: Status,
                                      response: String?) -> Unit)

    fun getAnn(completionHandler: (status: Status,
                                   response: ArrayList<AnnItem>?) -> Unit)

    fun getAnnDetail(bulletinId: String,
                     completionHandler: (status: Status,
                                         response: AnnItem?) -> Unit)

    fun cancelPendingRequests()
}