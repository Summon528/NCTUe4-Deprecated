package com.team214.nctue4.connect

import android.content.Context
import android.graphics.Color
import android.os.Parcelable
import android.util.Log
import com.team214.nctue4.R
import com.team214.nctue4.model.*
import com.team214.nctue4.utility.E3Type
import com.team214.nctue4.utility.MemberType
import com.team214.nctue4.utility.forceGetJsonArray
import com.team214.nctue4.utility.htmlCleaner
import fr.arnaudguyon.xmltojsonlib.XmlToJson
import kotlinx.android.parcel.Parcelize
import okhttp3.Call
import okhttp3.Callback
import okhttp3.FormBody
import okhttp3.OkHttpClient
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


@Parcelize
class OldE3Connect(private var studentId: String = "",
                   private var studentPassword: String = "",
                   private var loginTicket: String = "",
                   private var accountId: String = "") : OldE3Interface, Parcelable {

    companion object {
        private val tag = OldE3Connect::class.java.simpleName
        private const val loginPath = "/Login"
    }

    private val client = OkHttpClient().newBuilder().followRedirects(false)
            .followSslRedirects(false).build()

    private fun post(path: String, params: HashMap<String, String>,
                     secondTry: Boolean = false,
                     completionHandler: (status: OldE3Interface.Status,
                                         response: JSONObject?) -> Unit) {
        params["loginTicket"] = loginTicket
        params["studentId"] = accountId
        params["accountId"] = accountId
        if (loginTicket == "" && path != loginPath) {
            getLoginTicket { status, _ ->
                if (status == OldE3Interface.Status.SUCCESS) {
                    post(path, params, secondTry, completionHandler)
                } else {
                    completionHandler(status, null)
                }
            }
        } else {
            val url = "http://e3.nctu.edu.tw/mService/Service.asmx$path"
            Log.d("OldE3URL", url)
            val formBodyBuilder = FormBody.Builder()
            params.forEach { entry -> formBodyBuilder.add(entry.key, entry.value) }
            val formBody = formBodyBuilder.build()

            val request = okhttp3.Request.Builder().url(url).post(formBody).build()

            val call = client.newCall(request)

            call.enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    completionHandler(OldE3Interface.Status.SERVICE_ERROR, null)
                }

                override fun onResponse(call: Call, response: okhttp3.Response) {
                    if (response.code() == 500) { // the server explode if out login ticket expired
                        if (!secondTry && path != loginPath) {
                            getLoginTicket { _, _ ->
                                post(path, params, true, completionHandler)
                            }
                        } else completionHandler(OldE3Interface.Status.SERVICE_ERROR, null)
                    } else {
                        val xmlToJson = (XmlToJson.Builder(response.body().string()).build()).toJson()
                        completionHandler(OldE3Interface.Status.SUCCESS, xmlToJson)
                    }
                }
            })
        }
    }


    override fun getLoginTicket(completionHandler: (status: OldE3Interface.Status,
                                                    response: Pair<String, String>?) -> Unit) {
        post(loginPath, hashMapOf(
                "account" to studentId,
                "password" to studentPassword
        )) { status, response ->
            if (status == OldE3Interface.Status.SUCCESS) {
                val accountData = response!!.getJSONObject("AccountData")
                if (accountData.has("LoginTicket")) {
                    val studentName = accountData.getString("Name")
                    val studentEmail = accountData.getString("EMail")
                    loginTicket = accountData.getString("LoginTicket")
                    accountId = accountData.getString("AccountId")
                    completionHandler(OldE3Interface.Status.SUCCESS, Pair(studentName, studentEmail))
                } else {
                    completionHandler(OldE3Interface.Status.WRONG_CREDENTIALS, null)
                }
            } else {
                completionHandler(status, null)
            }
        }
    }

    override fun getCourseList(completionHandler: (status: OldE3Interface.Status,
                                                   response: ArrayList<CourseItem>?) -> Unit) {
        post("/GetCourseList", hashMapOf(
                "role" to "stu"
        )) { status, response ->
            if (status == OldE3Interface.Status.SUCCESS) {
                val data = response!!.getJSONObject("ArrayOfCourseData")
                        .forceGetJsonArray("CourseData")
                val courseItems = ArrayList<CourseItem>()
                (0 until data.length()).map { data.get(it) as JSONObject }
                        .forEach {
                            courseItems.add(CourseItem(it.getString("CourseNo"),
                                    it.getString("CourseName"),
                                    it.getString("TeacherName"),
                                    it.getString("CourseId"),
                                    E3Type.OLD))
                        }
                completionHandler(status, courseItems)
            } else {
                completionHandler(status, null)
            }
        }
    }

    override fun getAnnouncementListLogin(count: Int, completionHandler: (status: OldE3Interface.Status,
                                                                          response: ArrayList<AnnItem>?) -> Unit) {
        post("/GetAnnouncementList_LoginByCountWithAttach", hashMapOf(
                "ShowCount" to count.toString()
        )) { status, response ->
            if (status == OldE3Interface.Status.SUCCESS) {
                val annData = response!!.getJSONObject("ArrayOfBulletinData")
                        .forceGetJsonArray("BulletinData")
                val annItems = ArrayList<AnnItem>()
                val df = SimpleDateFormat("yyyy/M/d", Locale.TAIWAN)
                (0 until annData.length()).map { annData.get(it) as JSONObject }
                        .forEach {
                            val attachItemList = ArrayList<AttachItem>()
                            val attachNames = it.forceGetJsonArray("AttachFileName")
                            val attachUrls = it.forceGetJsonArray("AttachFileURL")
                            val attachFileSizes = it.forceGetJsonArray("AttachFileFileSize")
                            if ((attachNames.get(0) as JSONObject).getString("string") != "") {
                                (0 until attachNames.length()).map {
                                    AttachItem(
                                            (attachNames.get(it) as JSONObject).getString("string").dropLast(1),
                                            (attachFileSizes.get(it) as JSONObject).getString("string").dropLast(1),
                                            (attachUrls.get(it) as JSONObject).getString("string").dropLast(1))
                                }.forEach {
                                    attachItemList.add(it)
                                }
                            }
                            annItems.add(AnnItem(
                                    it.getString("BulletinId"),
                                    it.getString("CourseName"),
                                    it.getString("Caption"),
                                    it.getString("Content"),
                                    df.parse(it.getString("BeginDate")),
                                    df.parse(it.getString("EndDate")),
                                    it.getString("CourseId"),
                                    E3Type.OLD,
                                    attachItemList
                            ))
                        }
                completionHandler(status, annItems)
            } else {
                completionHandler(status, null)
            }
        }
    }

    override fun getCourseAnn(courseId: String, courseName: String,
                              completionHandler: (status: OldE3Interface.Status,
                                                  response: ArrayList<AnnItem>?) -> Unit) {
        post("/GetAnnouncementListWithAttach", hashMapOf(
                "courseId" to courseId,
                "bulType" to "1"
        )) { status, response ->
            if (status == OldE3Interface.Status.SUCCESS) {
                val arrayOfBulletinData = response!!.getJSONObject("ArrayOfBulletinData")
                val data = arrayOfBulletinData.forceGetJsonArray("BulletinData")
                val annItems = ArrayList<AnnItem>()
                val df = SimpleDateFormat("yyyy/M/d", Locale.TAIWAN)
                (0 until data.length()).map { data.get(it) as JSONObject }
                        .forEach {
                            val attachItemList = ArrayList<AttachItem>()
                            val attachNames = it.forceGetJsonArray("AttachFileName")
                            val attachUrls = it.forceGetJsonArray("AttachFileURL")
                            val attachFileSizes = it.forceGetJsonArray("AttachFileFileSize")
                            if ((attachNames.get(0) as JSONObject).getString("string") != "") {
                                (0 until attachNames.length()).map {
                                    AttachItem(
                                            (attachNames.get(it) as JSONObject).getString("string").dropLast(1),
                                            (attachFileSizes.get(it) as JSONObject).getString("string").dropLast(1),
                                            (attachUrls.get(it) as JSONObject).getString("string").dropLast(1))
                                }.forEach {
                                    attachItemList.add(it)
                                }
                            }
                            annItems.add(AnnItem(
                                    it.getString("BulletinId"),
                                    courseName,
                                    it.getString("Caption"),
                                    htmlCleaner(it.getString("Content")),
                                    df.parse(it.getString("BeginDate")),
                                    df.parse(it.getString("EndDate")),
                                    it.getString("CourseId"),
                                    E3Type.OLD,
                                    attachItemList
                            ))
                        }
                completionHandler(status, annItems)
            } else {
                completionHandler(status, null)
            }
        }
    }


    private lateinit var getMaterialDocListStatus: Array<Boolean>
    private var docGroupItems: ArrayList<DocGroupItem>? = null
    override fun getMaterialDocList(courseId: String, context: Context,
                                    completionHandler: (status: OldE3Interface.Status,
                                                        response: ArrayList<DocGroupItem>?) -> Unit) {
        docGroupItems = ArrayList()
        getMaterialDocListStatus = Array(2, { false })
        for (i in 0..1) {
            post("/GetMaterialDocList", hashMapOf(
                    "courseId" to courseId,
                    "docType" to i.toString()
            )) { status, response ->
                if (status == OldE3Interface.Status.SUCCESS) {
                    processMaterialDocList(i, response!!, context, completionHandler)
                } else {
                    completionHandler(status, null)
                }
            }
        }
    }


    private fun processMaterialDocList(which: Int, response: JSONObject, context: Context,
                                       completionHandler: (status: OldE3Interface.Status,
                                                           response: ArrayList<DocGroupItem>?) -> Unit) {

        val arrayOfMaterialDocData = response.getJSONObject("ArrayOfMaterialDocData")
        val data = arrayOfMaterialDocData.forceGetJsonArray("MaterialDocData")
        (0 until data.length()).map { data.get(it) as JSONObject }
                .forEach {
                    var dateArray: List<String> = it.getString("BeginDate").split("/")
                    docGroupItems!!.add(DocGroupItem(
                            it.getString("DisplayName"),
                            it.getString("DocumentId"),
                            it.getString("CourseId"),
                            if (which == 0) context.getString(R.string.course_doc_type_handout)
                            else context.getString(R.string.course_doc_type_reference)
                    ))
                }
        getMaterialDocListStatus[which] = true
        if (getMaterialDocListStatus[0] && getMaterialDocListStatus[1]) {
            docGroupItems?.sortByDescending { it.docType }
            completionHandler(OldE3Interface.Status.SUCCESS, docGroupItems)
            docGroupItems = null
        }
    }

    override fun getAttachFileList(documentId: String, courseId: String,
                                   completionHandler: (status: OldE3Interface.Status,
                                                       response: ArrayList<AttachItem>?) -> Unit) {
        post("/GetAttachFileList", hashMapOf(
                "resId" to documentId,
                "metaType" to "10", //No idea what is this for
                "courseId" to courseId
        )) { status, response ->
            if (status == OldE3Interface.Status.SUCCESS) {
                val data = response!!.getJSONObject("ArrayOfAttachFileInfoData")
                        .forceGetJsonArray("AttachFileInfoData")
                val attachItems = ArrayList<AttachItem>()
                (0 until data.length()).map { data.get(it) as JSONObject }
                        .forEach {
                            attachItems.add(AttachItem(
                                    it.getString("DisplayFileName"),
                                    it.getString("FileSize"),
                                    it.getString("RealityFileName")))
                        }
                completionHandler(status, attachItems)
            } else {
                completionHandler(status, null)
            }
        }
    }

    private lateinit var getMemberListStatus: Array<Boolean>
    private var memberItems: ArrayList<MemberItem>? = null
    override fun getMemberList(courseId: String,
                               completionHandler: (status: OldE3Interface.Status,
                                                   response: ArrayList<MemberItem>?) -> Unit) {
        getMemberListStatus = arrayOf(false, false, false)
        memberItems = ArrayList()
        post("/GetMemberList", hashMapOf(
                "loginTicket" to loginTicket,
                "courseId" to courseId,
                "role" to "tea"
        )) { status, response ->
            if (status == OldE3Interface.Status.SUCCESS) {
                getMemberListStatus[0] = true
                processMembers(0, response!!, completionHandler)
            } else completionHandler(status, null)
        }
        post("/GetMemberList", hashMapOf(
                "loginTicket" to loginTicket,
                "courseId" to courseId,
                "role" to "ta"
        )) { status, response ->
            if (status == OldE3Interface.Status.SUCCESS) {
                getMemberListStatus[1] = true
                processMembers(1, response!!, completionHandler)
            } else completionHandler(status, null)
        }
        post("/GetMemberList", hashMapOf(
                "loginTicket" to loginTicket,
                "courseId" to courseId,
                "role" to "stu"
        )) { status, response ->
            if (status == OldE3Interface.Status.SUCCESS) {
                getMemberListStatus[2] = true
                processMembers(2, response!!, completionHandler)
            } else completionHandler(status, null)
        }
    }

    private fun processMembers(which: Int, response: JSONObject,
                               completionHandler: (status: OldE3Interface.Status,
                                                   response: ArrayList<MemberItem>?) -> Unit) {
        val data = response.getJSONObject("ArrayOfAccountData").forceGetJsonArray("AccountData")
        when (which) {
            0 -> {
                (0 until data.length()).map { data.get(it) as JSONObject }
                        .forEach {
                            val type = if (it.getString("RoleName").contains("助教")) MemberType.TA else MemberType.TEA
                            memberItems!!.add(MemberItem(
                                    it.getString("Name"),
                                    it.getString("DepartId"),
                                    try {
                                        it.getString("EMail")
                                    } catch (e: JSONException) {
                                        ""
                                    }, type
                            ))
                        }
            }
            1, 2 -> {
                (0 until data.length()).map { data.get(it) as JSONObject }
                        .forEach {
                            memberItems!!.add(MemberItem(
                                    it.getString("Name"),
                                    it.getString("DepartId"),
                                    try {
                                        it.getString("EMail")
                                    } catch (e: JSONException) {
                                        ""
                                    }, if (which == 1) MemberType.STU else MemberType.AUDIT
                            ))
                        }
            }
        }
        if (getMemberListStatus[0] && getMemberListStatus[1] && getMemberListStatus[2]) {
            memberItems!!.sortBy { it.type }
            completionHandler(OldE3Interface.Status.SUCCESS, memberItems)
            memberItems = null
        }
    }

    override fun getScoreData(courseId: String,
                              completionHandler: (status: OldE3Interface.Status,
                                                  response: ArrayList<ScoreItem>?) -> Unit) {
        post("/GetScoreData", hashMapOf(
                "courseId" to courseId
        )) { status, response ->
            if (status == OldE3Interface.Status.SUCCESS) {
                val data = response!!.getJSONObject("ScoreData")
                val types = arrayOf("Office", "Exam", "Ques", "Hwk", "Discuss", "OneSelf",
                        "Score", "AdjustToScoreForAll", "Absence", "AdjustToScore", "Attendance", "FinalScore")
                val scoreItems = ArrayList<ScoreItem>()
                types.forEach {
                    if (data.has(it)) {
                        val scoreData = data.getJSONObject(it).forceGetJsonArray("ScoreItemData")
                        (0 until scoreData.length()).map { scoreData.get(it) as JSONObject }
                                .forEach {
                                    scoreItems.add(ScoreItem(it.getString("DisplayName"),
                                            it.getString("Score3")))
                                }
                    }
                }
                completionHandler(status, scoreItems)
            } else completionHandler(status, null)
        }

    }

    private lateinit var assignStatus: Array<Boolean>
    private var assignItems: ArrayList<AssignItem>? = null
    override fun getAssign(courseId: String,
                           completionHandler: (status: OldE3Interface.Status, response: ArrayList<AssignItem>?) -> Unit) {
        assignStatus = Array(4, { false })
        assignItems = ArrayList()
        for (i in 1..4) {
            post("/GetStuHomeworkList", hashMapOf(
                    "courseId" to courseId,
                    "listType" to i.toString()
            )) { status, response ->
                if (status == OldE3Interface.Status.SUCCESS) {
                    assignStatus[i - 1] = true
                    processAssign(response!!, completionHandler)
                } else completionHandler(status, null)
            }
        }
    }

    private fun processAssign(response: JSONObject, completionHandler:
    (status: OldE3Interface.Status, response: ArrayList<AssignItem>?) -> Unit) {
        val df = SimpleDateFormat("yyyy/M/d", Locale.TAIWAN)
        if (response.has("ArrayOfHomeworkData")) {
            val homeworkData = response.getJSONObject("ArrayOfHomeworkData").forceGetJsonArray("HomeworkData")
            (0 until homeworkData.length()).map { homeworkData.get(it) as JSONObject }
                    .forEach {
                        assignItems!!.add(AssignItem(
                                it.getString("DisplayName"),
                                it.getString("HomeworkId"),
                                df.parse(it.getString("BeginDate")),
                                df.parse(it.getString("EndDate")),
                                submitId = it.getString("HwkSubmitId")
                        ))
                    }
        }
        if (assignStatus.all { it }) {
            completionHandler(OldE3Interface.Status.SUCCESS, assignItems)
        }
    }

    private lateinit var assignDetailStatus: Array<Boolean>
    private var assignDetailItem: AssignItem? = null
    override fun getAssignDetail(assId: String, courseId: String, submitId: String,
                                 completionHandler: (status: OldE3Interface.Status,
                                                     response: AssignItem?) -> Unit) {
        assignDetailStatus = Array(3, { false })
        assignDetailItem = AssignItem()
        assignDetailItem!!.assignId = assId
        post("/GetHomeworkInfo", hashMapOf(
                "hwkId" to assId
        )) { status, response ->
            if (status == OldE3Interface.Status.SUCCESS) {
                assignDetailStatus[0] = true
                processAssignDetail(response!!, 0, completionHandler)
            } else completionHandler(status, null)
        }
        getAttachFileList(assId, courseId) { status, response ->
            if (status == OldE3Interface.Status.SUCCESS) {
                assignDetailStatus[1] = true
                processAssignDetail(response!!, 1, completionHandler)
            } else completionHandler(status, null)
        }
        if (submitId != "") {
            getAttachFileList(submitId, courseId) { status, response ->
                if (status == OldE3Interface.Status.SUCCESS) {
                    assignDetailStatus[2] = true
                    processAssignDetail(response!!, 2, completionHandler)
                } else completionHandler(status, null)
            }
        } else {
            assignDetailStatus[2] = true
            processAssignDetail(ArrayList<AttachItem>(), 2, completionHandler)
        }
    }

    private fun processAssignDetail(response: Any, which: Int,
                                    completionHandler: (status: OldE3Interface.Status,
                                                        response: AssignItem?) -> Unit) {
        when (which) {
            0 -> {
                val data = (response as JSONObject).getJSONObject("HomeworkData")
                val df = SimpleDateFormat("yyyy/M/d hh:mm:ss", Locale.TAIWAN)
                assignDetailItem!!.name = data.getString("DisplayName")
                assignDetailItem!!.content = data.getString("Content")
                assignDetailItem!!.startDate = df.parse(data.getString("BeginDate"))
                assignDetailItem!!.endDate = df.parse(data.getString("EndDate"))
            }
            1 -> {
                assignDetailItem!!.attachItem = response as ArrayList<AttachItem>
            }
            2 -> {
                assignDetailItem!!.sentItem = response as ArrayList<AttachItem>
            }
        }
        if (assignDetailStatus.all { it }) completionHandler(OldE3Interface.Status.SUCCESS, assignDetailItem)
    }


    private lateinit var timeTableStatus: Array<Boolean>
    private var timeTableItems: Array<ArrayList<TimeTableItem>>? = null
    private val colorStringArray = mutableListOf("#E57373", "#F48FB1", "#CE93D8", "#9FA8DA", "#03A9F4", "#26A69A",
            "#4CAF50", "#AED581", "#AFB42B", "#FFA000", "#FFF176", "#FF5722", "#BCAAA4", "#90A4AE", "#90CAF9", "#E0E0E0")

    override fun getTimeTable(courses: ArrayList<CourseItem>,
                              completionHandler: (status: OldE3Interface.Status,
                                                  response: Array<ArrayList<TimeTableItem>>?) -> Unit) {
        timeTableStatus = Array(courses.size, { false })
        timeTableItems = Array(7, { ArrayList<TimeTableItem>() })
        colorStringArray.shuffle()
        courses.forEachIndexed { index, courseItem ->
            post("/GetCourseTime", hashMapOf(
                    "courseId" to courseItem.courseId
            )) { status, response ->
                if (status == OldE3Interface.Status.SUCCESS) {
                    processTimeTable(index, response!!, completionHandler)
                } else completionHandler(status, null)
            }
        }
    }

    private fun processTimeTable(which: Int, response: JSONObject,
                                 completionHandler: (status: OldE3Interface.Status,
                                                     response: Array<ArrayList<TimeTableItem>>?) -> Unit) {
        if (response.getJSONObject("ArrayOfCourseTimeData").has("CourseTimeData")) {
            val data = response.getJSONObject("ArrayOfCourseTimeData").forceGetJsonArray("CourseTimeData")
            (0 until data.length()).map { data.get(it) as JSONObject }.forEach {
                var weekDay = it.getInt("WeekDay")
                val section = it.getString("Section").single()
                val roomNo = it.getString("RoomNo").trim()
                val courseName = it.getString("CourseName")
                if (weekDay == 7) weekDay = 0
                val sectionInt = when (section) {
                    'M', 'N' -> section.minus('M')
                    'A', 'B', 'C', 'D' -> section.minus('A') + 2
                    'Y' -> 11
                    'X' -> 6
                    'E', 'F', 'G', 'H' -> section.minus('A') + 3
                    else -> section.minus('A') + 4
                }
                timeTableItems!![weekDay].add(TimeTableItem(
                        courseName,
                        weekDay,
                        sectionInt,
                        roomNo,
                        1,
                        Color.parseColor(colorStringArray[which % colorStringArray.size])
                ))
            }
        }
        timeTableStatus[which] = true
        if (timeTableStatus.all { it }) {
            timeTableItems!!.forEach { it.sortBy { it.section } }
            val timeTableItemsResult = Array(7, { ArrayList<TimeTableItem>() })
            timeTableItems!!.forEachIndexed { weekDay, arrayList ->
                var idx = 0
                while (idx < arrayList.size) {
                    var courseLength = 0
                    val curr = arrayList[idx]
                    while (idx < arrayList.size && curr.courseName == arrayList[idx].courseName) {
                        courseLength++
                        idx++
                    }
                    curr.length = courseLength
                    timeTableItemsResult[weekDay].add(curr)
                }
            }
            timeTableItems = null
            completionHandler(OldE3Interface.Status.SUCCESS, timeTableItemsResult)
        }

    }

    override fun cancelPendingRequests() {
        client.dispatcher().cancelAll()
    }
}

