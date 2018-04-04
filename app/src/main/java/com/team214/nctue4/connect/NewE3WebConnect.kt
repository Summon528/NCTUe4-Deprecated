package com.team214.nctue4.connect

import android.os.Parcelable
import android.util.Log
import com.team214.nctue4.model.AnnItem
import com.team214.nctue4.model.AttachItem
import com.team214.nctue4.utility.E3Type
import kotlinx.android.parcel.Parcelize
import okhttp3.*
import org.jsoup.Jsoup
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Locale
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
        if (cookieStore[HOST] == null && path != "/login/index.php?lang=en") {
            getCookie { status, response ->
                if (status == NewE3WebInterface.Status.SUCCESS) {
                    post(path, params, secondTry, completionHandler)
                } else completionHandler(status, null)
            }
        } else {
            val url = "https://e3new.nctu.edu.tw$path"
            Log.d("NewE3URL", url)
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
        post("/my/index.php?lang=en", HashMap()
        ) { status, response ->
            if (status == NewE3WebInterface.Status.SUCCESS) {
                val annPage = Jsoup.parse(response).select("#pc-for-in-progress")[0].select(" .course-info-container .hidden-xs-down")
                val annItems = ArrayList<AnnItem>()
                val df = SimpleDateFormat("d LLL,  yyyy", Locale.US)
                (0 until annPage.size).map { annPage[it] as org.jsoup.nodes.Element }
                        .forEach {
                            if (it.select("b").text() != "System") {
                                annItems.add(AnnItem(
                                        it.select("a").attr("href").substring(25) + "&lang=en",
                                        it.select("b").text().substring(10).replace(" .*".toRegex(), ""),
                                        it.select("h4").text(),
                                        it.select("a").text(),
                                        df.parse(it.select(".media div")[0].text().substring(0, 20).replace("([0-9]+[.|:,][0-9]*)".toRegex(), "") + "2018"),
                                        df.parse(it.select(".media div")[0].text().substring(0, 20).replace("([0-9]+[.|:,][0-9]*)".toRegex(), "") + "2018"),
                                        "",
                                        E3Type.NEW,
                                        ArrayList()
                                ))
                            }

                        }
                completionHandler(NewE3WebInterface.Status.SUCCESS, annItems)
            } else {
                completionHandler(status, null)
            }
        }
    }

    override fun getAnnDetail(bulletinId: String, completionHandler: (status: NewE3WebInterface.Status, response: AnnItem?) -> Unit) {
        post(bulletinId, HashMap()
        ) { status, response ->
            if (status == NewE3WebInterface.Status.SUCCESS) {
                val annPage = Jsoup.parse(response)
                val df = SimpleDateFormat("EEEE, d MMMM yyyy", Locale.US)
                val caption = if (annPage.select(".name").size > 0) {
                    annPage.select(".name").text().substring(5)
                } else {
                    annPage.select(".subject").text().substring(5)
                }
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
                        Regex("courseid=([^&]*)").find(annPage.select(".list-group-item-action")[1].attr("href"))!!.groupValues[1],
                        E3Type.NEW,
                        attachItems
                )
                completionHandler(NewE3WebInterface.Status.SUCCESS, annItem)
            } else {
                completionHandler(status, null)
            }
        }
    }

    override fun cancelPendingRequests() {
        client.dispatcher().cancelAll()
    }
}

