package com.example.codytseng.nctue4.utility

import android.content.Context
import android.preference.PreferenceManager
import android.util.Log
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import fr.arnaudguyon.xmltojsonlib.XmlToJson
import java.util.*

/**
 * Created by codytseng on 2018/3/13.
 */

class OldE3Connect : OldE3Interface {
    private var loginTicket : String = ""

    override fun getLoginTicket(context: Context, completionHandler: (status: OldE3Interface.Status) -> Unit) {
        val queue = Volley.newRequestQueue(context)
        val url = "http://e3.nctu.edu.tw/mService/service.asmx/Login"
        val stringRequest = object : StringRequest(Request.Method.POST, url,
                Response.Listener<String> { response ->
                    val xmlToJson = XmlToJson.Builder(response).build().toJson()
                    val accountData = xmlToJson!!.getJSONObject("AccountData")
                    if (accountData.has("LoginTicket")) {
                        loginTicket = accountData.getString("LoginTicket")
                        completionHandler(OldE3Interface.Status.SUCCESS)
                    } else {
                        completionHandler(OldE3Interface.Status.WRONG_CREDENTIALS)
                    }
                },
                Response.ErrorListener {
                    completionHandler(OldE3Interface.Status.SERVICE_ERROR)
                }) {
            override fun getParams(): Map<String, String> {
                val prefs = PreferenceManager.getDefaultSharedPreferences(context)
                val params = HashMap<String, String>()
                params.put("account", prefs.getString("studentId", ""))
                params.put("password", prefs.getString("studentPassword", ""))
                return params
            }
        }
        queue.add(stringRequest)
    }

    override fun loginSetup(studentId: String, studentPassword: String, context: Context,
                            completionHandler: (status: OldE3Interface.Status,
                                                response: Pair<String, String>?) -> Unit) {
        val queue = Volley.newRequestQueue(context)
        val url = "http://e3.nctu.edu.tw/mService/service.asmx/Login"
        val stringRequest = object : StringRequest(Request.Method.POST, url,
                Response.Listener<String> { response ->
                    val xmlToJson = XmlToJson.Builder(response).build().toJson()
                    val accountData = xmlToJson!!.getJSONObject("AccountData")
                    if (accountData.has("Name")) {
                        Log.d("QQ", accountData.toString())
                        val studentName = accountData.getString("Name")
                        val studentEmail =
                                if (accountData.has("EMail")) {
                                    accountData.getString("EMail")
                                } else {
                                    "No Email"
                                }
                        completionHandler(OldE3Interface.Status.SUCCESS, Pair(studentName, studentEmail))
                    } else {
                        completionHandler(OldE3Interface.Status.WRONG_CREDENTIALS, null)
                    }
                },
                Response.ErrorListener {
                    completionHandler(OldE3Interface.Status.SERVICE_ERROR, null)
                }) {
            override fun getParams(): Map<String, String> {
                val params = HashMap<String, String>()
                params.put("account", studentId)
                params.put("password", studentPassword)
                return params
            }
        }
        queue.add(stringRequest)
    }

}