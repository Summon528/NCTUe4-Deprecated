package com.example.codytseng.nctue4.model

/**
 * Created by CodyTseng on 3/13/2018.
 */
class CourseItem(courseNo: Int, courseName:String, teacherName: String, courseId : String) {
    var mTeacherName: String = teacherName
    var mCourseName: String = courseName
    var mCourseNo: Int = courseNo
    var mCourseId = courseId
}