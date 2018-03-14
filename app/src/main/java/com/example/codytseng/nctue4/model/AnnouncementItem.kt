package com.example.codytseng.nctue4.model

/**
 * Created by s094392 on 3/14/18.
 */
class AnnouncementItem(bulType: Int, bulletinId: String, courseName: String, caption: String, content: String, beginDate: String, endDate: String) {
    var mBulType: Int = bulType
    var mBulletinId: String = bulletinId
    var mCourseName: String = courseName
    var mCaption: String = caption
    var mContent: String = content
    var mBeginDate: String = beginDate
    var mEndDate: String = endDate
}