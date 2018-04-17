package com.team214.nctue4.utility

import com.crashlytics.android.Crashlytics

fun logLong(level: Int, tag: String, content: String, e: Exception? = null) {
    if (content.length > 4000) {
        val chunkCount: Int = content.length / 4000
        for (i in 0..chunkCount) {
            val max = 4000 * (i + 1)
            if (max >= content.length) {
                Crashlytics.log(level, tag + i.toString(), content.substring(4000 * i))
            } else {
                Crashlytics.log(level, tag + i.toString(), content.substring(4000 * i, max))
            }
        }
    } else Crashlytics.log(level, tag, content)
    if (e != null) Crashlytics.logException(e)
}