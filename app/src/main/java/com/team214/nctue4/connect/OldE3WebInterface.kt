package com.team214.nctue4.connect

import com.team214.nctue4.model.AnnItem

interface OldE3WebInterface {
    enum class Status {
        SUCCESS, SERVICE_ERROR, WRONG_CREDENTIALS
    }

    fun getAnn(completionHandler: (status: Status,
                                   response: ArrayList<AnnItem>?) -> Unit)

    fun cancelPendingRequests()
}