package com.team214.nctue4.connect

import android.annotation.SuppressLint
import android.os.Parcelable
import android.util.Log
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.team214.nctue4.model.CourseItem
import com.team214.nctue4.utility.E3Type
import kotlinx.android.parcel.Parcelize
import org.json.JSONArray
import org.json.JSONObject
import java.util.*
import kotlin.collections.ArrayList


@Parcelize
@SuppressLint("ParcelCreator")
class NewE3Connect(private var studentId: String = "",
                   private var studentPassword: String = "",
                   private var userId: String = "",
                   private var token: String = "") : NewE3Interface, Parcelable {

    private val tag = NewE3Connect::class.java.simpleName

    private fun post(path: String?, params: HashMap<String, String>,
                     secondTry: Boolean = false,
                     completionHandler: (status: NewE3Interface.Status,
                                         response: String?) -> Unit) {
        val url = if (path == null) "https://e3new.nctu.edu.tw/webservice/rest/server.php?moodlewsrestformat=json"
        else "https://e3new.nctu.edu.tw$path"
        Log.d("NewE3ApiURL", url)
        val stringRequest = object : StringRequest(Request.Method.POST, url,
                Response.Listener<String> { response ->
                    completionHandler(NewE3Interface.Status.SUCCESS, response)
                },
                Response.ErrorListener { _ ->
                    if (!secondTry && path != "/login/token.php") {
                        getToken { _, _ ->
                            post(path, params, true, completionHandler)
                        }
                    } else completionHandler(NewE3Interface.Status.SERVICE_ERROR, null)
                }) {
            override fun getParams(): Map<String, String> {
                return params
            }
        }
        VolleyHandler.instance?.addToRequestQueue(stringRequest, tag)
    }


    override fun getToken(completionHandler: (status: NewE3Interface.Status, response: String?) -> Unit) {
        post("/login/token.php", hashMapOf(
                "username" to studentId,
                "password" to studentPassword,
                "service" to "moodle_mobile_app"
        )) { status, response ->
            if (status == NewE3Interface.Status.SUCCESS) {
                val data = JSONObject(response)
                if (data.has("token")) {
                    token = data.getString("token")
                    completionHandler(NewE3Interface.Status.SUCCESS, token)
                } else {
                    completionHandler(NewE3Interface.Status.WRONG_CREDENTIALS, null)
                }
            } else {
                completionHandler(status, null)
            }
        }
    }

    override fun getUserId(completionHandler: (status: NewE3Interface.Status, response: String?) -> Unit) {
        post(null, hashMapOf(
                "wsfunction" to "core_webservice_get_site_info",
                "wstoken" to token
        )) { status, response ->
            if (status == NewE3Interface.Status.SUCCESS) {
                userId = JSONObject(response).getString("userid")
                completionHandler(NewE3Interface.Status.SUCCESS, userId)
            } else {
                completionHandler(status, null)
            }
        }
    }

    override fun getCourseList(completionHandler: (status: NewE3Interface.Status,
                                                   response: ArrayList<CourseItem>?) -> Unit) {
        post(null, hashMapOf(
                "wsfunction" to "core_enrol_get_users_courses",
                "wstoken" to token,
                "userid" to userId
        )) { status, response ->
            if (status == NewE3Interface.Status.SUCCESS) {
                val data = JSONArray(response)
                val courseItems = ArrayList<CourseItem>()
                (0 until data.length()).map { data.get(it) as JSONObject }
                        .forEach {
                            val split = it.getString("fullname").split(".")
                            courseItems.add(CourseItem(split[1],
                                    split[2],
                                    "",
                                    it.getString("id"),
                                    E3Type.NEW))
                        }
                completionHandler(status, courseItems)
            } else {
                completionHandler(status, null)
            }
        }
    }

    override fun cancelPendingRequests() {
        VolleyHandler.instance?.cancelPendingRequests(tag)
    }
}

