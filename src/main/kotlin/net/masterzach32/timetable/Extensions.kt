package net.masterzach32.timetable

import com.mashape.unirest.http.Unirest
import net.masterzach32.timetable.obj.Section
import org.jsoup.Jsoup
import org.jsoup.select.Elements

fun Section.pullSectionComments(): Section.Comments {
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
            Section.Comments(
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

private infix fun Elements.meetingTable(label: String): List<Section.MeetingTime> {
    return this.first { it.getElementsByClass("pllabel")?.text()?.contains(label) ?: false }
            .let { table ->
                table.getElementsByTag("tr").drop(2)
                        .map { row ->
                            row.children().drop(1).map { it.text().replace("\n", "") }.let {
                                if (it.size > 5)
                                    Section.MeetingTime(it[4], it[5], it[6], it[7])
                                else
                                    Section.MeetingTime(it[0], it[1], it[2], it[3])
                            }
                        }
            }
}

private infix fun Elements.instructor(label: String): String {
    return this.first { it.getElementsByClass("pllabel")?.text()?.contains(label) ?: false }
            .getElementsByTag("tr").drop(3).first().children().first().text()
}