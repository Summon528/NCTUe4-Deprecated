package com.team214.nctue4.connect

import android.os.Parcelable
import android.util.Log
import com.crashlytics.android.Crashlytics
import com.team214.nctue4.model.AnnItem
import com.team214.nctue4.model.AttachItem
import com.team214.nctue4.utility.E3Type
import com.team214.nctue4.utility.logLong
import kotlinx.android.parcel.Parcelize
import okhttp3.*
import org.jsoup.Jsoup
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

@Parcelize
class NewE3WebConnect(private var studentId: String = "",
                      private var studentPassword: String = "",
                      private var newE3Cookie: String = "") : NewE3WebInterface, Parcelable {

    companion object {
        private const val HOST = "e3new.nctu.edu.tw"
        private const val loginPath = "/login/index.php?lang=en"
    }

    fun returnE3Cookie() = cookieStore[HOST]

    private val client = OkHttpClient().newBuilder().followRedirects(false)
            .followSslRedirects(false).cookieJar(
                    object : CookieJar {
                        override fun loadForRequest(url: HttpUrl?): MutableList<Cookie>? {
                            return if (cookieStore[HOST] != null) cookieStore[HOST]
                            else mutableListOf()
                        }

                        override fun saveFromResponse(url: HttpUrl?, cookies: MutableList<Cookie>?) {
                            cookieStore[HOST] =
                                    if (cookies!!.size > 1) cookies.subList(1, 2)
                                    else cookies
                        }
                    }).build()

    private var cookieStore: HashMap<String, MutableList<Cookie>> = if (newE3Cookie != "") {
        hashMapOf(HOST to mutableListOf(Cookie.parse(HttpUrl.parse("https://e3new.nctu.edu.tw/"),
                "MoodleSession=$newE3Cookie")))
    } else {
        hashMapOf()
    }

    private fun post(path: String, params: HashMap<String, String>,
                     secondTry: Boolean = false,
                     completionHandler: (status: NewE3WebInterface.Status,
                                         response: String?) -> Unit) {
        if (cookieStore[HOST] == null && path != loginPath) {
            getCookie { status, _ ->
                if (status == NewE3WebInterface.Status.SUCCESS) {
                    post(path, params, secondTry, completionHandler)
                } else completionHandler(status, null)
            }
        } else {
            val url = "https://e3new.nctu.edu.tw$path"
            Crashlytics.log(Log.DEBUG, "NewWebE3URL", url)
            val formBody = FormBody.Builder()
                    .add("username", studentId)
                    .add("password", studentPassword).build()

            val request = Request.Builder().url(url).post(formBody).build()

            val call = client.newCall(request)

            call.enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    completionHandler(NewE3WebInterface.Status.SERVICE_ERROR, null)
                }

                override fun onResponse(call: Call, response: Response) {
                    val res = response.body().string()
                    if (res.contains("This page should automatically redirect. If nothing is happening please use the continue link below.<br /><a href=\"https://e3new.nctu.edu.tw/login/index.php\">Continue</a>") ||
                            res.contains("本頁面會自動重新導向。如果什麼都沒發生，請點選下面的\"繼續\"連結。<br /><a href=\"https://e3new.nctu.edu.tw/login/index.php\">繼續")) {
                        if (!secondTry && path != "/login/index.php?lang=en") {
                            getCookie { _, _ ->
                                post(path, params, true, completionHandler)
                            }
                        } else completionHandler(NewE3WebInterface.Status.WRONG_CREDENTIALS, null)
                    } else completionHandler(NewE3WebInterface.Status.SUCCESS, res)
                }
            })
        }
    }

    override fun getCookie(completionHandler: (status: NewE3WebInterface.Status, response: String?) -> Unit) {
        cookieStore.clear()
        post(loginPath, hashMapOf(
                "username" to studentId,
                "password" to studentPassword
        )) { status, response ->
            if (status == NewE3WebInterface.Status.SUCCESS) {
                completionHandler(NewE3WebInterface.Status.SUCCESS, response)
            } else {
                completionHandler(status, null)
            }
        }
    }

    override fun getAnn(completionHandler: (status: NewE3WebInterface.Status, response: ArrayList<AnnItem>?) -> Unit) {
        post("/theme/dcpc/news/index.php?lang=en", HashMap()
        ) { status, response ->
            if (status == NewE3WebInterface.Status.SUCCESS) {
                try {
                    if (response!!.contains("This page should automatically redirect. If nothing is happening please use the continue link below.<br /><a href=\"https://e3new.nctu.edu.tw/user/edit.php")) {
                        completionHandler(NewE3WebInterface.Status.NOT_INIT, null)
                    } else {
                        val annPage = Jsoup.parse(response).select(".NewsRow")
                        val annItems = ArrayList<AnnItem>()
                        val df = SimpleDateFormat("d MMM, HH:mm", Locale.US)
                        (0 until annPage.size).map { annPage[it] as org.jsoup.nodes.Element }
                                .forEach {
                                    if (it.select(".NewsPages").size != 0) return@forEach
                                    if (it.select(".colL-10").text() != "System") {
                                        val date = df.parse(it.select(".colR-10")[0].text())
                                        val now = Calendar.getInstance()
                                        val nowMonth = now.get(Calendar.MONTH)
                                        val nowYear = now.get(Calendar.YEAR) - 1900
                                        // 嘗試猜公告的年份為何
                                        date.year =
                                                if (nowMonth >= 9 && date.month <= 2) nowYear + 1
                                                else if (nowMonth <= 2 && date.month >= 9) nowYear - 1
                                                else nowYear
                                        annItems.add(AnnItem(
                                                it.select("div").attr("onclick")
                                                        .removePrefix("location.href='https://e3new.nctu.edu.tw").removeSuffix("';") + "&lang=en",
                                                it.select(".colL-10").attr("title").split("\\xa0".toRegex())[1].split(" ")[0],
                                                it.select(".colL-19").text(),
                                                "",
                                                date,
                                                date,
                                                "",
                                                E3Type.NEW,
                                                ArrayList()
                                        ))
                                    }

                                }

                        completionHandler(NewE3WebInterface.Status.SUCCESS, annItems)
                    }
                } catch (e: Exception) {
                    logLong(Log.ERROR, "NewE3WebError", response!!, e)
                    completionHandler(NewE3WebInterface.Status.SERVICE_ERROR, null)
                }
            } else {
                completionHandler(status, null)
            }
        }
    }

    override fun getAnnDetail(bulletinId: String, completionHandler: (status: NewE3WebInterface.Status, response: AnnItem?) -> Unit) {
        post(bulletinId, HashMap()
        ) { status, response ->
            if (status == NewE3WebInterface.Status.SUCCESS) {
                try {
                    val annPage = Jsoup.parse(response)
                    val df = SimpleDateFormat("EEEE, d MMMM yyyy", Locale.US)
                    val caption = if (annPage.select(".name").size > 0) {
                        annPage.select(".name").text()
                    } else {
                        annPage.select(".subject").text()
                    }.removePrefix("&nbsp").trimStart()
                    val attachItems = ArrayList<AttachItem>()
                    annPage.select(".attachments").forEach {
                        attachItems.add(AttachItem(
                                it.select("a").last().text(),
                                "?",
                                it.select("a").last().attr("href")
                        ))
                    }
                    val annItem = AnnItem(
                            bulletinId,
                            annPage.select(".page-header-headings").text().replace("【.*】\\d*".toRegex(), "").replace(" .*".toRegex(), ""),
                            caption,
                            annPage.select(".content").html(),
                            df.parse(annPage.select(".author").text().replace(", \\d+:\\d+.*".toRegex(), "")),
                            df.parse(annPage.select(".author").text().replace(", \\d+:\\d+.*".toRegex(), "")),
                            Regex("(courseid|id)=([^&]*)").find(annPage.select(".list-group-item-action")[3].attr("href"))!!.groupValues[2],
                            E3Type.NEW,
                            attachItems
                    )
                    completionHandler(NewE3WebInterface.Status.SUCCESS, annItem)
                } catch (e: Exception) {
                    logLong(Log.ERROR, "NewE3WebError", response!!, e)
                    completionHandler(NewE3WebInterface.Status.SERVICE_ERROR, null)
                }
            } else {
                completionHandler(status, null)
            }
        }
    }

    override fun cancelPendingRequests() {
        client.dispatcher().cancelAll()
    }
}

