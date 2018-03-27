package com.team214.nctue4.model

class CourseItem(val courseNo: String,
                 val courseName: String,
                 val teacherName: String,
                 val courseId: String,
                 val e3Type: Int,
                 val bookmarked: Int = 0,
                 val bookmarkIdx: Int = 10000,
                 val idx: Int = 10000
)