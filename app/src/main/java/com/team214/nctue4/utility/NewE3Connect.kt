package com.team214.nctue4.utility

import android.util.Log
import com.team214.nctue4.model.AnnItem
import okhttp3.*
import org.jsoup.Jsoup
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Locale
import kotlin.collections.ArrayList
import kotlin.collections.HashMap


class NewE3Connect(private var studentId: String = "",
                   private var studentPassword: String = "",
                   private var newE3Cookie: String = "") : NewE3Interface {


    private val HOST = "e3new.nctu.edu.tw"
    private var cookieStore: HashMap<String, MutableList<Cookie>> = if (newE3Cookie != "") {
        hashMapOf(HOST to mutableListOf(Cookie.parse(HttpUrl.parse("https://e3new.nctu.edu.tw/"),
                "MoodleSession=$newE3Cookie")))
    } else {
        hashMapOf()
    }

    override fun getCredential(): String {
        Log.d("POSTTGET", cookieStore[HOST]!![0].value())
        return cookieStore[HOST]!![0].value()
    }


    private fun post(path: String, params: HashMap<String, String>,
                     secondTry: Boolean = false,
                     completionHandler: (status: NewE3Interface.Status,
                                         response: String?) -> Unit) {

        Log.d("POSTT", path)
        if (cookieStore[HOST] != null)
            Log.d("POSTTCOOKIE", cookieStore[HOST]!![0].value().toString())
        val client = OkHttpClient().newBuilder().cache(null).followRedirects(false)
                .followSslRedirects(false).cookieJar(object : CookieJar {
                    override fun loadForRequest(url: HttpUrl?): MutableList<Cookie>? {
                        val tmp = if (cookieStore[HOST] != null) cookieStore[HOST]
                        else mutableListOf()
                        Log.d("POSTTSENTT", tmp.toString())
                        return tmp
                    }

                    override fun saveFromResponse(url: HttpUrl?, cookies: MutableList<Cookie>?) {
                        Log.d("POSTTGET", cookies.toString())
                        cookieStore[HOST] =
                                if (cookies!!.size > 1) cookies.subList(1, 2)
                                else cookies
                    }
                }).build()
        val url = "https://e3new.nctu.edu.tw$path"
//        Log.d("user an pas", studentId + " " + studentPassword)

        val formBody = FormBody.Builder()
                .add("username", studentId)
                .add("password", studentPassword).build()

        val request = Request.Builder().url(url).post(formBody).build()

        val call = client.newCall(request)

        call.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                completionHandler(NewE3Interface.Status.SERVICE_ERROR, null)
            }

            override fun onResponse(call: Call, response: Response) {
                val res = response.body().string()
                Log.d("POSTTRES", res)
                if (res.contains("<title>New E3 數位教學平台: 登入本網站</title>") ||
                        res.contains("<title>New E3 數位教學平台: Log in to the site</title>") ||
                        res.contains("This page should automatically redirect. If nothing is happening please use the continue link below.<br /><a href=\"https://e3new.nctu.edu.tw/login/index.php\">Continue</a>") ||
                        res.contains("本頁面會自動重新導向。如果什麼都沒發生，請點選下面的\"繼續\"連結。<br /><a href=\"https://e3new.nctu.edu.tw/login/index.php\">繼續")) {
//                    Log.d("fail", cookieStore.toString())
                    cookieStore.clear()
//                    newE3Cookie = ""
                    if (!secondTry && path != "/login/index.php?lang=en") {
                        Log.d("secondTry", "secondTry")
                        getCookie { _, _ ->
                            post(path, params, true, completionHandler)
                        }
                    } else completionHandler(NewE3Interface.Status.WRONG_CREDENTIALS, null)
                }
//                if (res.contains("pc-for-in-progress")) {
//                    Log.d("con", "yes")
//                }
                else completionHandler(NewE3Interface.Status.SUCCESS, res)
            }
        })

    }

    override fun getCookie(completionHandler: (status: NewE3Interface.Status, response: String?) -> Unit) {
        cookieStore.clear()
        post("/login/index.php?lang=en", hashMapOf(
                "username" to studentId,
                "password" to studentPassword
        )) { status, response ->
            if (status == NewE3Interface.Status.SUCCESS) {
                completionHandler(NewE3Interface.Status.SUCCESS, response)
            } else {
                completionHandler(status, null)
            }
        }
    }

    override fun getAnn(completionHandler: (status: NewE3Interface.Status, response: ArrayList<AnnItem>?) -> Unit) {
        post("/my/index.php?lang=en", HashMap()
        ) { status, response ->
            if (status == NewE3Interface.Status.SUCCESS) {
                Log.d("loca", response!!)
                if (response.contains("pc-for-in-progress")) {
                    Log.d("con", "yes")
                }
                val annPage = Jsoup.parse(response).select("#pc-for-in-progress")[0].select(" .course-info-container .hidden-xs-down")
                var annItems = ArrayList<AnnItem>()
                val df = SimpleDateFormat("d LLL,  yyyy", Locale.US)
                (0 until annPage.size).map { annPage[it] as org.jsoup.nodes.Element }
                        .forEach {
                            if (it.select("b").text() != "System") {
                                annItems.add(AnnItem(
                                        1,
                                        it.select("a").attr("href").substring(25) + "&lang=en",
                                        it.select("b").text().substring(10).replace(" .*".toRegex(), ""),
                                        it.select("h4").text(),
                                        it.select("a").text(),
                                        df.parse(it.select(".media div")[0].text().substring(0, 20).replace("([0-9]+[\\.|\\:,][0-9]*)".toRegex(), "") + "2018"),
                                        df.parse(it.select(".media div")[0].text().substring(0, 20).replace("([0-9]+[\\.|\\:,][0-9]*)".toRegex(), "") + "2018"),
                                        "",
                                        ArrayList()
                                ))
                            }

                        }
                completionHandler(NewE3Interface.Status.SUCCESS, annItems)
            } else {
                completionHandler(status, null)
            }
        }
    }

    override fun getAnnDetail(bulletinId: String, completionHandler: (status: NewE3Interface.Status, response: AnnItem?) -> Unit) {
        Log.d("buul id", bulletinId)
        post(bulletinId, HashMap()
        ) { status, response ->
            //            Log.d("detail cookie", cookie)
            if (status == NewE3Interface.Status.SUCCESS) {
                Log.d("getanndetail", response)
                val annPage = Jsoup.parse(response)
                val df = SimpleDateFormat("EEEE, d MMMM yyyy", Locale.US)
                val caption = if (annPage.select(".name").size > 0) {
                    annPage.select(".name").text().substring(5)
                } else {
                    annPage.select(".subject").text().substring(5)
                }
                val annItem = AnnItem(
                        1,
                        bulletinId,
                        annPage.select(".page-header-headings").text().replace("【.*】\\d*".toRegex(), "").replace(" .*".toRegex(), ""),
                        caption,
                        annPage.select(".content").html(),
                        df.parse(annPage.select(".author").text().replace(", \\d+:\\d+.*".toRegex(), "")),
                        df.parse(annPage.select(".author").text().replace(", \\d+:\\d+.*".toRegex(), "")),
                        "",
                        ArrayList()
                )
                completionHandler(NewE3Interface.Status.SUCCESS, annItem)
            } else {
                completionHandler(status, null)
            }
        }
    }

//    override fun cancelPendingRequests() {
//        VolleyHandler.instance?.cancelPendingRequests(tag)
//    }
}

