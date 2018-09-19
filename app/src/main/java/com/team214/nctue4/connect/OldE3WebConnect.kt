package com.team214.nctue4.connect

import android.os.Parcelable
import com.team214.nctue4.model.AnnItem
import com.team214.nctue4.utility.E3Type
import kotlinx.android.parcel.Parcelize
import okhttp3.*
import org.jsoup.Jsoup
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

@Parcelize
class OldE3WebConnect(private var studentId: String = "",
                      private var studentPassword: String = "") : OldE3WebInterface, Parcelable {

    companion object {
        private const val HOST = "https://e3.nctu.edu.tw"
    }

    private val cookieStore: HashMap<String, MutableList<Cookie>> = hashMapOf()
    private val client = OkHttpClient().newBuilder()
            .cookieJar(object : CookieJar {
                override fun loadForRequest(url: HttpUrl?): MutableList<Cookie>? {
                    return if (cookieStore[HOST] != null) cookieStore[HOST]
                    else mutableListOf()
                }

                override fun saveFromResponse(url: HttpUrl?, cookies: MutableList<Cookie>) {
                    cookieStore[HOST] = cookies
                }
            }).build()


    override fun getAnn(completionHandler: (status: OldE3WebInterface.Status, response: ArrayList<AnnItem>?) -> Unit) {
        cookieStore.clear()
        val request = Request.Builder()
                .url("https://dcpc.nctu.edu.tw/index.aspx")
                .build()
        val call = client.newCall(request)
        call.enqueue(object : Callback {
            override fun onResponse(call: Call, response: Response) {
                val ele = Jsoup.parse(response.body()!!.string())
                cookieStore.clear()
                val formBody = FormBody.Builder()
                        .add("__EVENTTARGET", "")
                        .add("__EVENTARGUMENT", "")
                        .add("__VIEWSTATE", ele.select("input[name=__VIEWSTATE]").attr("value"))
                        .add("__VIEWSTATEGENERATOR", ele.select("input[name=__VIEWSTATEGENERATOR]").attr("value"))
                        .add("__VIEWSTATEENCRYPTED", "")
                        .add("__EVENTVALIDATION", ele.select("input[name=__EVENTVALIDATION]").attr("value"))
                        .add("rblLang", "zh-TW")
                        .add("txtAccount", studentId)
                        .add("txtPwd", studentPassword)
                        .add("btnLoginIn", "SIGN IN").build()

                val request = Request.Builder()
                        .url("https://dcpc.nctu.edu.tw/index.aspx")
                        .post(formBody)
                        .build()


                val call = client.newCall(request)

                call.enqueue(object : Callback {
                    override fun onResponse(call: Call, response: Response) {
                        val result = response.body()!!.string()
                        if (result.contains("登入帳號不存在或密碼錯誤")) {
                            completionHandler(OldE3WebInterface.Status.WRONG_CREDENTIALS, ArrayList())
                            return
                        }
                        val ele = Jsoup.parse(result)
                        val baseSelector = "html > body > form#aspnetForm > table > tbody > tr > td > table > tbody > tr > td > table > tbody > tr > td > div > table > tbody > tr > td"
                        val title = ele.select("$baseSelector > a")
                        val timeAndCourse = ele.select("$baseSelector > span")
                        val content = ele.select("$baseSelector > div")

                        val annItems = ArrayList<AnnItem>()

                        val df = SimpleDateFormat("yyyy/MM/dd", Locale.US)
                        var otherIdx = 0
                        for (tcIdx in 0 until timeAndCourse.size) {
                            if (timeAndCourse[tcIdx].text() == "(重要公告)") continue
                            annItems.add(AnnItem(
                                    "",
                                    timeAndCourse[tcIdx].text().split("【")[1].removeSuffix("】"),
                                    title[otherIdx].text(),
                                    content[otherIdx].html(),
                                    df.parse(timeAndCourse[tcIdx].text().split("【")[0]),
                                    df.parse(timeAndCourse[tcIdx].text().split("【")[0]),
                                    "",
                                    E3Type.OLD,
                                    ArrayList()
                            ))
                            otherIdx += 1
                        }
                        completionHandler(OldE3WebInterface.Status.SUCCESS, annItems)
                    }

                    override fun onFailure(call: Call, e: IOException) {
                        completionHandler(OldE3WebInterface.Status.SERVICE_ERROR, ArrayList())
                    }
                })
            }

            override fun onFailure(call: Call, e: IOException) {
                completionHandler(OldE3WebInterface.Status.SERVICE_ERROR, ArrayList())
            }
        })


    }


    override fun cancelPendingRequests() {
        client.dispatcher().cancelAll()
    }
}


