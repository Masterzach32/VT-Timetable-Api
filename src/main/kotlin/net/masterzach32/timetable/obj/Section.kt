package net.masterzach32.timetable.obj

import com.mashape.unirest.http.Unirest
import net.masterzach32.timetable.Timetable
import net.masterzach32.timetable.TimetableException
import org.jsoup.Jsoup
import org.jsoup.select.Elements

/*
 * VT-Timetable-Api - Created on 8/7/2018
 * Author: Zach Kozar
 * 
 * This code is licensed under the GNU GPL v3
 * You can find more info in the LICENSE file at the project root.
 */

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

    fun pullSectionComments(): Comments {
        try {
            val request = Unirest.post(Timetable.COMMENTS_URL)
            request.fields(
                    mutableMapOf<String, Any>(
                            "CRN" to crn,
                            "TERM" to term.month.id,
                            "YEAR" to term.year,
                            "SUBJ" to subjectCode,
                            "CRSE" to courseNumber,
                            "history" to "N"
                    )
            )

            val doc = Jsoup.parse(request.asString().body)

            return doc.selectFirst(".plaintable").getElementsByTag("tr").let {
                Comments(
                        it value "Catalog Description",
                        it meetingTable "Meeting Times",
                        it instructor "Section Info",
                        it value "Comments",
                        it value "Cross-Listed with",
                        it value "Linked with",
                        it value "Curriculum",
                        it value "Prerequisites",
                        it value "Corequisites",
                        it value "Major",
                        it value "Minor",
                        it value "College",
                        it value "Level",
                        it value "Class",
                        it value "Campus",
                        it value "Degree",
                        it value "Program",
                        it value "Hours",
                        it value "GPA",
                        it value "Attribute"
                )
            }

        } catch (e: Exception) {
            e.printStackTrace()
            throw TimetableException("Could not pull section comments for $subjectCode-$courseNumber with CRN $crn: $e")
        }
    }

    private infix fun Elements.value(label: String): String {
        return this.first { it.getElementsByClass("pllabel")?.text()?.contains(label) ?: false }
                .getElementsByClass("pldefault").text()
    }

    private infix fun Elements.meetingTable(label: String): List<MeetingTime> {
        return this.first { it.getElementsByClass("pllabel")?.text()?.contains(label) ?: false }
                .let { table ->
                    table.getElementsByTag("tr").drop(2)
                            .map { row ->
                                row.children().drop(1).map { it.text().replace("\n", "") }.let {
                                    if (it.size > 5)
                                        MeetingTime(it[4], it[5], it[6], it[7])
                                    else
                                        MeetingTime(it[0], it[1], it[2], it[3])
                                }
                            }
                }
    }

    private infix fun Elements.instructor(label: String): String {
        return this.first { it.getElementsByClass("pllabel")?.text()?.contains(label) ?: false }
                .getElementsByTag("tr").drop(3).first().children().first().text()
    }

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