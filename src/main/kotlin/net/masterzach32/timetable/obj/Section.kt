package net.masterzach32.timetable.obj

import com.mashape.unirest.http.Unirest
import net.masterzach32.timetable.Timetable
import net.masterzach32.timetable.TimetableException
import org.jsoup.Jsoup
import org.jsoup.select.Elements

/**
 * @author Zach Kozar
 * @version 8/7/2018
 */
data class Section(
        val crn: String,
        val subjectCode: String,
        val courseNumber: String,
        val name: String,
        val term: Term,
        val campus: Campus,
        val sectionType: SectionType,
        val credits: String,
        val capacity: Int,
        val instructor: String,
        val meeting: MeetingTime,
        val examType: String,
        val additionalTimes: MeetingTime? = null,
        val comments: String? = null
) {

    val commentsUrl = Timetable.COMMENTS_URL + "?CRN=$crn&TERM=${term.month.id}&YEAR=${term.year}&SUBJ=$subjectCode&CRSE=$courseNumber&history=N"

    data class MeetingTime(
            val days: String,
            val startTime: String,
            val endTime: String,
            val location: String
    )

    data class Comments(
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
}