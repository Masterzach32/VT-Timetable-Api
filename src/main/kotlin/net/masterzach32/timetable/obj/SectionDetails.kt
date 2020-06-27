package net.masterzach32.timetable.obj

data class SectionDetails(
    val description: String,
    val meetingTimes: List<MeetingTime>,
    val instructor: String,
    val comments: String,
    val crossLinkedWith: String,
    val linkedWith: String,
    val curriculum: String,
    val prerequisites: String,
    val corequisites: String,
    val major: String,
    val minor: String,
    val college: String,
    val level: String,
    val `class`: String,
    val campus: String,
    val degree: String,
    val program: String,
    val hours: String,
    val gpa: String,
    val attribute: String
)
