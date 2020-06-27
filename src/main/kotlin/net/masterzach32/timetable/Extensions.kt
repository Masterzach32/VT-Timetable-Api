package net.masterzach32.timetable

import io.ktor.client.request.*
import io.ktor.client.statement.*
import net.masterzach32.timetable.obj.*
import org.jsoup.*
import org.jsoup.select.*

val Section.commentsUrl: String
    get() = Timetable.COMMENTS_URL + "?CRN=$crn&TERM=${term.month.id}&YEAR=${term.year}&SUBJ=$subjectCode&CRSE=$courseNumber&history=N"

suspend fun Section.pullSectionDetails(): SectionDetails {
    try {
//        val params = parametersOf(
//            "CRN" to listOf(crn),
//            "TERM" to listOf(term.month.id),
//            "YEAR" to listOf(term.year),
//            "SUBJ" to listOf(subjectCode),
//            "CRSE" to listOf(courseNumber),
//            "history" to listOf("N")
//        )
        val response: HttpResponse = Timetable.httpClient.get(commentsUrl)

        return Jsoup.parse(response.readText()).selectFirst(".plaintable").getElementsByTag("tr").let {
            SectionDetails(
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