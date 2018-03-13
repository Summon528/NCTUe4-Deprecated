package com.example.codytseng.nctue4.utility

import android.content.Context

/**
 * Created by CodyTseng on 3/13/2018.
 */


interface OldE3Interface {
    enum class Status {
        SUCCESS, SERVICE_ERROR, WRONG_CREDENTIALS
    }

    fun loginSetup(studentId: String, studentPassWord: String, context: Context,
                   completionHandler: (status: OldE3Interface.Status,
                                       response: Pair<String, String>?) -> Unit)

    fun getLoginTicket(context: Context, completionHandler: (status: OldE3Interface.Status) -> Unit)
}