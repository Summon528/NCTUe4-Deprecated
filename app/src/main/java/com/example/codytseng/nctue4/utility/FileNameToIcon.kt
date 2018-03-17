package com.example.codytseng.nctue4.utility

import com.example.codytseng.nctue4.R

class FileNameToIcon {

    fun getId(fileName: String): Int {
        val key = fileName.split(".").last()
        return if (key in iconMap) iconMap[key]!! else R.drawable.file_alt
    }

    companion object {
        val iconMap = hashMapOf(
                "png" to R.drawable.file_image,
                "jpg" to R.drawable.file_image,
                "jpeg" to R.drawable.file_image,
                "gif" to R.drawable.file_image,
                "bmp" to R.drawable.file_image,
                "tif" to R.drawable.file_image,
                "tiff" to R.drawable.file_image,
                "svg" to R.drawable.file_image,
                "7z" to R.drawable.file_archive,
                "rar" to R.drawable.file_archive,
                "zip" to R.drawable.file_archive,
                "gz" to R.drawable.file_archive,
                "deb" to R.drawable.file_archive,
                "rpm" to R.drawable.file_archive,
                "pkg" to R.drawable.file_archive,
                "mp3" to R.drawable.file_audio,
                "wav" to R.drawable.file_audio,
                "wma" to R.drawable.file_audio,
                "wpa" to R.drawable.file_audio,
                "aif" to R.drawable.file_audio,
                "cda" to R.drawable.file_audio,
                "mid" to R.drawable.file_audio,
                "midi" to R.drawable.file_audio,
                "ogg" to R.drawable.file_audio,
                "cpp" to R.drawable.file_code,
                "c" to R.drawable.file_code,
                "py" to R.drawable.file_code,
                "java" to R.drawable.file_code,
                "kt" to R.drawable.file_code,
                "js" to R.drawable.file_code,
                "ts" to R.drawable.file_code,
                "json" to R.drawable.file_code,
                "xml" to R.drawable.file_code,
                "htm" to R.drawable.file_code,
                "html" to R.drawable.file_code,
                "xls" to R.drawable.file_excel,
                "xlsx" to R.drawable.file_excel,
                "ods" to R.drawable.file_excel,
                "pdf" to R.drawable.file_pdf,
                "xls" to R.drawable.file_pdf,
                "ppt" to R.drawable.file_powerpoint,
                "pptx" to R.drawable.file_powerpoint,
                "pps" to R.drawable.file_powerpoint,
                "ppsx" to R.drawable.file_powerpoint,
                "odp" to R.drawable.file_powerpoint,
                "doc" to R.drawable.file_word,
                "docx" to R.drawable.file_word,
                "odt" to R.drawable.file_word
        )
    }
}