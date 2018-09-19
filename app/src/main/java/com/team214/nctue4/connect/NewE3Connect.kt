package com.team214.nctue4.connect

import android.content.Context
import android.os.Parcelable
import android.preference.PreferenceManager
import android.util.Log
import com.crashlytics.android.Crashlytics
import com.team214.nctue4.R
import com.team214.nctue4.model.*
import com.team214.nctue4.utility.E3Type
import com.team214.nctue4.utility.MemberType
import com.team214.nctue4.utility.logLong
import kotlinx.android.parcel.IgnoredOnParcel
import kotlinx.android.parcel.Parcelize
import okhttp3.Call
import okhttp3.Callback
import okhttp3.FormBody
import okhttp3.OkHttpClient
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.util.*
import kotlin.collections.ArrayList


@Parcelize
class NewE3Connect(private var studentId: String = "",
                   private var studentPassword: String = "",
                   private var userId: String = "",
                   private var token: String = "") : NewE3Interface, Parcelable {

    @IgnoredOnParcel
    var context: Context? = null

    companion object {
        private const val loginPath = "/login/token.php"
    }

    @IgnoredOnParcel
    private val client = OkHttpClient().newBuilder().followRedirects(false)
            .followSslRedirects(false).build()

    private fun post(path: String?, params: HashMap<String, String>,
                     secondTry: Boolean = false,
                     completionHandler: (status: NewE3Interface.Status,
                                         response: String?) -> Unit) {
        params["wstoken"] = token
        if (params.contains("userid")) params["userid"] = userId
        if (token == "" && path != loginPath) {
            getToken { status, _ ->
                if (status == NewE3Interface.Status.SUCCESS) {
                    getUserId { status, _ ->
                        if (status == NewE3Interface.Status.SUCCESS)
                            post(path, params, secondTry, completionHandler)
                        else completionHandler(status, null)
                    }
                } else completionHandler(status, null)
            }
        } else {
            val url = if (path == loginPath) "https://e3new.nctu.edu.tw$path"
            else "https://e3new.nctu.edu.tw/webservice/rest/server.php?moodlewsrestformat=json"
            Crashlytics.log(Log.DEBUG, "NewE3URL", url)
            val formBodyBuilder = FormBody.Builder()
            params.forEach { entry -> formBodyBuilder.add(entry.key, entry.value) }
            val formBody = formBodyBuilder.build()

            val request = okhttp3.Request.Builder().url(url).post(formBody).build()

            val call = client.newCall(request)

            call.enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    completionHandler(NewE3Interface.Status.SERVICE_ERROR, null)
                }

                override fun onResponse(call: Call, response: okhttp3.Response) {
                    val res = response.body().string()
                    try {
                        val resJson = JSONObject(res)
                        if (resJson.has("errorcode") && resJson.getString("errorcode") == "invalidtoken") {
                            getToken { status, resToken ->
                                if (status == NewE3Interface.Status.SUCCESS) {
                                    val prefs = PreferenceManager.getDefaultSharedPreferences(context)
                                    prefs.edit().putString("newE3Token", token).apply()
                                    token = resToken!!
                                    post(path, params, secondTry, completionHandler)
                                } else completionHandler(status, null)
                            }
                            return
                        }
                    } catch (e: JSONException) {
                    }
                    completionHandler(NewE3Interface.Status.SUCCESS, res)
                }
            })
        }
    }


    override fun getToken(completionHandler: (status: NewE3Interface.Status, response: String?) -> Unit) {
        post("/login/token.php", hashMapOf(
                "username" to studentId,
                "password" to studentPassword,
                "service" to "moodle_mobile_app"
        )) { status, response ->
            if (status == NewE3Interface.Status.SUCCESS) {
                try {
                    val data = JSONObject(response)
                    if (data.has("token")) {
                        token = data.getString("token")
                        completionHandler(NewE3Interface.Status.SUCCESS, token)
                    } else {
                        completionHandler(NewE3Interface.Status.WRONG_CREDENTIALS, null)
                    }
                } catch (e: Exception) {
                    logLong(Log.ERROR, "NewE3Error", response!!, e)
                    completionHandler(NewE3Interface.Status.SERVICE_ERROR, null)
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
                try {
                    val jsonObject = JSONObject(response)
                    userId = jsonObject.getString("userid")
                    val name = jsonObject.getString("firstname")

                    val prefs = PreferenceManager.getDefaultSharedPreferences(context)
                    prefs.edit().putString("studentName", name).apply()
                    completionHandler(NewE3Interface.Status.SUCCESS, userId)
                } catch (e: Exception) {
                    logLong(Log.ERROR, "NewE3Error", response!!, e)
                    completionHandler(NewE3Interface.Status.SERVICE_ERROR, null)
                }
            } else {
                completionHandler(status, null)
            }
        }
    }

    override fun getCourseList(completionHandler: (status: NewE3Interface.Status,
                                                   response: ArrayList<CourseItem>?) -> Unit) {
        post(null, hashMapOf(
                "wsfunction" to "core_enrol_get_users_courses",
                "userid" to userId
        )) { status, response ->
            if (status == NewE3Interface.Status.SUCCESS) {
                try {
                    val data = JSONArray(response)
                    val courseItems = ArrayList<CourseItem>()
                    (0 until data.length()).map { data.get(it) as JSONObject }
                            .forEach {
                                if (it.getLong("enddate") > System.currentTimeMillis() / 1000) {
                                    val split = it.getString("fullname").split(".")
                                    val courseName: String
                                    val courseNo: String
                                    if (split.size >= 3) {
                                        courseNo = split[1]
                                        courseName = split[2]
                                    } else {
                                        courseNo = ""
                                        courseName = split[0]
                                    }
                                    courseItems.add(
                                            CourseItem(
                                                    courseNo,
                                                    courseName,
                                                    it.getString("shortname"),
                                                    it.getString("id"),
                                                    E3Type.NEW
                                            )
                                    )
                                }
                            }
                    completionHandler(status, courseItems)
                } catch (e: Exception) {
                    logLong(Log.ERROR, "NewE3Error", response!!, e)
                    completionHandler(NewE3Interface.Status.SERVICE_ERROR, null)
                }
            } else {
                completionHandler(status, null)
            }
        }
    }

    override fun getCourseAnn(courseId: String, courseName: String, completionHandler: (status: NewE3Interface.Status, response: ArrayList<AnnItem>?) -> Unit) {
        post(null, hashMapOf(
                "wsfunction" to "mod_forum_get_forums_by_courses",
                "courseids[0]" to courseId
        )) { status, response ->
            if (status == NewE3Interface.Status.SUCCESS) {
                try {
                    val data = JSONArray(response)
                    val forumId = (data.get(0) as JSONObject).getString("id")
                    post(null, hashMapOf(
                            "wsfunction" to "mod_forum_get_forum_discussions_paginated",
                            "forumid" to forumId,
                            "sortdirection" to "DESC",
                            "perpage" to "100",
                            "sortby" to "timemodified"
                    )) { status2, response2 ->
                        if (status2 == NewE3Interface.Status.SUCCESS) {
                            try {
                                val data2 = JSONObject(response2).getJSONArray("discussions")
                                val courseAnnItems = ArrayList<AnnItem>()
                                (0 until data2.length()).map { data2.get(it) as JSONObject }.forEach {
                                    courseAnnItems.add(AnnItem(
                                            "",
                                            courseName,
                                            it.getString("name"),
                                            it.getString("message"),
                                            Date(it.getLong("timemodified") * 1000),
                                            Date(it.getLong("timeend") * 1000),
                                            courseId,
                                            E3Type.NEW,
                                            arrayListOf()
                                    ))
                                }
                                courseAnnItems.sortByDescending { it.beginDate }
                                completionHandler(NewE3Interface.Status.SUCCESS, courseAnnItems)
                            } catch (e: Exception) {
                                logLong(Log.ERROR, "NewE3Error", response!!, e)
                                completionHandler(NewE3Interface.Status.SERVICE_ERROR, null)
                            }
                        } else completionHandler(status, null)
                    }
                } catch (e: Exception) {
                    logLong(Log.ERROR, "NewE3Error", response!!, e)
                    completionHandler(NewE3Interface.Status.SERVICE_ERROR, null)
                }
            } else completionHandler(status, null)
        }
    }

    override fun getCourseFolder(courseId: String, context: Context,
                                 completionHandler: (status: NewE3Interface.Status, response: ArrayList<DocGroupItem>?) -> Unit) {
        post(null, hashMapOf(
                "wsfunction" to "mod_folder_get_folders_by_courses",
                "courseids[0]" to courseId
        )) { status, response ->
            if (status == NewE3Interface.Status.SUCCESS) {
                try {
                    val data = JSONObject(response).getJSONArray("folders")
                    val docGroupItems = ArrayList<DocGroupItem>()
                    (0 until data.length()).map { data.get(it) as JSONObject }.forEach {
                        var name = it.getString("name")
                        val which = if (name.startsWith("[參考資料]")) {
                            name = name.drop(6)
                            1
                        } else 0
                        docGroupItems.add(DocGroupItem(
                                name,
                                it.getString("coursemodule"),
                                courseId,
                                if (which == 0) context.getString(R.string.course_doc_type_handout)
                                else context.getString(R.string.course_doc_type_reference)
                        ))
                    }
                    completionHandler(status, docGroupItems)
                } catch (e: Exception) {
                    logLong(Log.ERROR, "NewE3Error", response!!, e)
                    completionHandler(NewE3Interface.Status.SERVICE_ERROR, null)
                }
            } else completionHandler(status, null)
        }
    }

    override fun getFiles(courseId: String, folderId: String,
                          completionHandler: (status: NewE3Interface.Status,
                                              response: ArrayList<AttachItem>?) -> Unit) {

        post(null, hashMapOf(
                "courseid" to courseId,
                "options[0][name]" to "cmid",
                "options[0][value]" to folderId,
                "wsfunction" to "core_course_get_contents"
        )) { status, response ->
            if (status == NewE3Interface.Status.SUCCESS) {
                try {
                    val data = JSONArray(response)
                    val attachItems = ArrayList<AttachItem>()

                    (0 until data.length()).map { data.get(it) as JSONObject }.forEach {
                        if (it.getJSONArray("modules").length() > 0) {
                            val data2 = it.getJSONArray("modules").getJSONObject(0).getJSONArray("contents")
                            (0 until data2.length()).map { data2.get(it) as JSONObject }.forEach {
                                attachItems.add(AttachItem(
                                        it.getString("filename"),
                                        it.getString("filesize"),
                                        it.getString("fileurl") + "&token=$token"
                                ))
                            }
                        }
                    }
                    completionHandler(status, attachItems)
                } catch (e: Exception) {
                    logLong(Log.ERROR, "NewE3Error", response!!, e)
                    completionHandler(NewE3Interface.Status.SERVICE_ERROR, null)
                }
            } else completionHandler(status, null)
        }
    }

    override fun getMemberList(courseId: String,
                               completionHandler: (status: NewE3Interface.Status,
                                                   response: ArrayList<MemberItem>?) -> Unit) {
        post(null, hashMapOf(
                "courseid" to courseId,
                "wsfunction" to "core_enrol_get_enrolled_users"
        )) { status, response ->
            if (status == NewE3Interface.Status.SUCCESS) {
                try {
                    val memberItems = ArrayList<MemberItem>()
                    val data = JSONArray(response)
                    (0 until data.length()).map { data.get(it) as JSONObject }
                            .forEach {
                                val roles = it.getJSONArray("roles")
                                val type =
                                //They don't provide role information sometimes, let's just assume it's student
                                        if (roles.length() > 0) {
                                            when (it.getJSONArray("roles").getJSONObject(0).getInt("roleid")) {
                                                5 -> MemberType.STU
                                                9 -> MemberType.TA
                                                3 -> MemberType.TEA
                                                else -> MemberType.STU
                                            }
                                        } else MemberType.STU

                                memberItems.add(MemberItem(
                                        it.getString("fullname").split(" ").last(),
                                        it.getString("fullname").split(" ").first(),
                                        try {
                                            it.getString("email")
                                        } catch (e: JSONException) {
                                            ""
                                        }, type
                                ))

                            }
                    memberItems.sortBy { it.type }
                    completionHandler(status, memberItems)
                } catch (e: Exception) {
                    logLong(Log.ERROR, "NewE3Error", response!!, e)
                    completionHandler(NewE3Interface.Status.SERVICE_ERROR, null)
                }
            } else completionHandler(status, null)
        }

    }

    override fun getScoreData(courseId: String,
                              completionHandler: (status: NewE3Interface.Status,
                                                  response: ArrayList<ScoreItem>?) -> Unit) {
        post(null, hashMapOf(
                "courseid" to courseId,
                "userid" to userId,
                "wsfunction" to "gradereport_user_get_grades_table"
        )) { status, response ->
            if (status == NewE3Interface.Status.SUCCESS) {
                try {
                    val scoreItems = ArrayList<ScoreItem>()
                    val data = JSONObject(response!!).getJSONArray("tables")
                            .getJSONObject(0).getJSONArray("tabledata")
                    for (i in 1 until data.length()) {
                        val tmp =
                                try {
                                    data.getJSONObject(i)
                                } catch (e: JSONException) {
                                    continue
                                }
                        scoreItems.add(ScoreItem(
                                Regex("/<([^>]*)").find(tmp.getJSONObject("itemname").getString("content").reversed())
                                !!.groupValues.last().reversed(),
                                tmp.getJSONObject("grade").getString("content")
                        ))
                    }
                    completionHandler(status, scoreItems)
                } catch (e: Exception) {
                    logLong(Log.ERROR, "NewE3Error", response!!, e)
                    completionHandler(NewE3Interface.Status.SERVICE_ERROR, null)
                }
            } else completionHandler(status, null)
        }
    }

    override fun getAssign(courseId: String,
                           completionHandler: (status: NewE3Interface.Status,
                                               response: ArrayList<AssignItem>?) -> Unit) {
        post(null, hashMapOf(
                "courseids[0]" to courseId,
                "wsfunction" to "mod_assign_get_assignments"
        )) { status, response ->
            if (status == NewE3Interface.Status.SUCCESS) {
                try {
                    val data = JSONObject(response).getJSONArray("courses").getJSONObject(0)
                            .getJSONArray("assignments")
                    val assignItems = ArrayList<AssignItem>()
                    (0 until data.length()).map { data.get(it) as JSONObject }.forEach {
                        val attachItems = ArrayList<AttachItem>()
                        val attachItemJson = it.getJSONArray("introattachments")
                        (0 until attachItemJson.length()).map { attachItemJson.get(it) as JSONObject }
                                .forEach {
                                    attachItems.add(AttachItem(
                                            it.getString("filename"),
                                            it.getString("filesize"),
                                            it.getString("fileurl") + "?token=$token"
                                    ))
                                }
                        assignItems.add(AssignItem(
                                it.getString("name"),
                                it.getString("id"),
                                Date(it.getLong("allowsubmissionsfromdate") * 1000),
                                Date(it.getLong("duedate") * 1000),
                                it.getString("intro"),
                                attachItems
                        ))
                    }
                    assignItems.sortByDescending { it.startDate }
                    completionHandler(status, assignItems)
                } catch (e: Exception) {
                    logLong(Log.ERROR, "NewE3Error", response!!, e)
                    completionHandler(NewE3Interface.Status.SERVICE_ERROR, null)
                }
            } else completionHandler(status, null)
        }


    }

    override fun getAssignSubmission(assignId: String,
                                     completionHandler: (status: NewE3Interface.Status,
                                                         response: ArrayList<AttachItem>?) -> Unit) {
        post(null, hashMapOf(
                "assignid" to assignId,
                "userid" to userId,
                "wsfunction" to "mod_assign_get_submission_status"
        )) { status, response ->
            if (status == NewE3Interface.Status.SUCCESS) {
                try {
                    val attachItems = ArrayList<AttachItem>()
                    val data = JSONObject(response).getJSONObject("lastattempt").getJSONObject("submission")
                            .getJSONArray("plugins").getJSONObject(0).getJSONArray("fileareas")
                            .getJSONObject(0).getJSONArray("files")
                    (0 until data.length()).map { data.get(it) as JSONObject }
                            .forEach {
                                attachItems.add(AttachItem(
                                        it.getString("filename"),
                                        it.getString("filesize"),
                                        it.getString("fileurl") + "?token=$token"
                                ))
                            }
                    completionHandler(status, attachItems)
                } catch (e: Exception) {
                    logLong(Log.ERROR, "NewE3Error", response!!, e)
                    completionHandler(NewE3Interface.Status.SERVICE_ERROR, null)
                }
            } else completionHandler(status, null)
        }
    }

    override fun cancelPendingRequests() {
        client.dispatcher().cancelAll()
    }
}

