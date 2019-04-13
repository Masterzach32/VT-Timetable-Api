package net.masterzach32.timetable

import net.masterzach32.timetable.obj.*
import com.mashape.unirest.http.Unirest
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.slf4j.LoggerFactory
import java.net.URL
import java.util.*

object Timetable {

    private val logger = LoggerFactory.getLogger(this.javaClass)
    const val GET_URL = "https://banweb.banner.vt.edu/ssb/prod/HZSKVTSC.P_DispRequest"
    const val POST_URL = "https://banweb.banner.vt.edu/ssb/prod/HZSKVTSC.P_ProcRequest"
    const val COMMENTS_URL = "https://banweb.banner.vt.edu/ssb/prod/HZSKVTSC.P_ProcComments"
    private const val OPEN_ONLY_DEFAULT = false

    @JvmStatic
    @JvmOverloads
    fun lookupCrn(
            crn: String,
            term: Term = getCurrentTerm(),
            openOnly: Boolean = OPEN_ONLY_DEFAULT
    ): Section? {
        return lookup(
                crn = crn,
                term = term,
                openOnly = openOnly
        ).firstOrNull()
    }

    @JvmStatic
    @JvmOverloads
    fun lookupCourse(
            subjectCode: String,
            courseNumber: String,
            instructor: String? = null,
            curriculum: Curriculum = Curriculum.All,
            term: Term = getCurrentTerm(),
            openOnly: Boolean = OPEN_ONLY_DEFAULT
    ): List<Section> {
        return lookup(
                subjectCode = subjectCode,
                courseNumber = courseNumber,
                instructor = instructor,
                curriculum = curriculum,
                term = term,
                openOnly = openOnly
        )
    }

    @JvmStatic
    fun lookup(
            crn: String? = null,
            subjectCode: String? = null,
            courseNumber: String? = null,
            instructor: String? = null,
            curriculum: Curriculum = Curriculum.All,
            term: Term = getCurrentTerm(),
            campus: Campus = Campus.BLACKSBURG,
            sectionType: SectionType? = null,
            openOnly: Boolean = OPEN_ONLY_DEFAULT
    ): List<Section> {
        val requestParams = getDefaultRequestParams()

        requestParams["CAMPUS"] = campus.id
        requestParams["TERMYEAR"] = term.code
        requestParams["CORE_CODE"] = curriculum.code

        if (crn != null) {
            if (crn.length < 3)
                throw TimetableException("Invalid CRN: must be longer than 3 characters.")
            requestParams["crn"] = crn
        }

        if (subjectCode != null)
            requestParams["subj_code"] = subjectCode

        if (courseNumber != null) {
            if (courseNumber.length != 4)
                throw TimetableException("Invalid Class Number: must be 4 characters.")
            requestParams["CRSE_NUMBER"] = courseNumber
        }

        if (subjectCode == null && courseNumber != null) {
            throw TimetableException("A subject code must be supplied with a class number. (ie. ENGL 1105)")
        }

        if (sectionType != null)
            requestParams["SCHDTYPE"] = sectionType.id

        requestParams["open_only"] = if (openOnly) "on" else ""

        val sections = parseTable(makeRequest(requestParams), term, campus)

        return sections.filter {
            if (instructor != null)
                it.instructor.toLowerCase().contains(instructor.toLowerCase())
            else
                true
        }
    }

    @JvmStatic
    fun getAvailableTerms(): List<Term> {
        return try {
            Jsoup.parse(URL(GET_URL), 5000).body()
                    .getElementsByAttributeValue("name", "TERMYEAR")
                    .first().allElements
                    .drop(2)
                    .mapNotNull { it.attr("value") }
                    .distinct()
                    .map { Term(it) }
        } catch (e: Exception) {
            e.printStackTrace()
            throw TimetableException("Encountered an error while fetching available terms: $e")
        }
    }

    @JvmStatic
    fun getCurrentTerm(): Term {
        return Calendar.getInstance().run {
            Term(Session.forMonth(get(Calendar.MONTH).toString()), get(Calendar.YEAR).toString())
        }
    }

    @JvmStatic
    @JvmOverloads
    fun getAllSubjects(term: Term = getCurrentTerm()): List<String> {
        val requestParams = getDefaultRequestParams()

        requestParams["CAMPUS"] = Campus.BLACKSBURG.id
        requestParams["TERMYEAR"] = term.code

        try {
            val doc = makeRequest(requestParams)

            val reg = "^(case \"${term.code}\" :.+?break;)".toRegex(setOf(RegexOption.DOT_MATCHES_ALL, RegexOption.MULTILINE))
            val result = reg.find(doc.getElementsByTag("script").toString())!!
            val block = result.groupValues.first().split("\n").drop(2)

            val reg2 = "\"([A-z0-9]{2,4})\"".toRegex()

            return block.map {
                val res = reg2.find(it)
                if (res != null)
                    res.groupValues[1]
                else
                    ""
            }.filter { it.isNotEmpty() }
        } catch (e: Exception) {
            e.printStackTrace()
            throw TimetableException("Encountered an error while fetching available subjects: $e")
        }
    }

    private fun makeRequest(requestParams: Map<String, Any>): Document {
        val request = Unirest.post(POST_URL)
        request.fields(requestParams)
        request.header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/60.0.3112.113 Safari/537.36")

        val response = try {
            request.asString()
        } catch (t: Throwable) {
            logger.error("Could not read response from timetable: ${t.message}")
            throw t
        }

        if (response.status != 200)
            throw TimetableException("Could not access timetable! Received status code ${response.status}")

        return Jsoup.parse(response.body)
    }

    private fun parseTable(html: Document, term: Term, campus: Campus): List<Section> {
        val table = html.selectFirst(".dataentrytable") ?: return emptyList()
        val sections = mutableListOf<Section>()
        val rows = table.getElementsByTag("tr").drop(1)

        rows.indices.forEach {
            val row = rows[it]
            try {
                val additionalData = when {
                    it + 2 < rows.size -> listOf(rows[it+1], rows[it+2])
                    it + 1 < rows.size -> listOf(rows[it+1])
                    else -> emptyList()
                }

                parseSection(row, additionalData, term, campus)
                        .also { section -> if (section != null) sections.add(section) }
            } catch (e: Exception) {
                logger.warn("Encountered ${e.javaClass.simpleName} when trying to parse row " +
                        "\"${row.text()}\": ${e.message}. Skipping. Please make an issue on Github if you " +
                        "believe this is an error with the API.")
                e.printStackTrace()
            }
        }
        return sections
    }

    private fun parseSection(tableRow: Element, additionalData: List<Element>, term: Term, campus: Campus): Section? {
        val rowText = tableRow.getElementsByTag("td").text()
        val rowElements = tableRow.getElementsByTag("td").map { it.text().replace("\n", "") }
        if (rowText.contains("* Additional Times *") || rowText.contains("Comments for CRN"))
            return null

        val arr = rowElements.any { it.contains("(ARR)") }

        val crn = rowElements[0]
        val code = rowElements[1].split("-")
        val subjectCode = code[0]
        val courseNumber = code[1]
        val name = rowElements[2]
        val sectionType = SectionType.forId(rowElements[3])
        val credits = rowElements[4]
        val capacity = rowElements[5].toInt()
        val instructor = rowElements[6]

        val days: String
        val startTime: String
        val endTime: String
        val location: String
        val examType: String
        if (rowElements.size < 10) {
            days = "N/A"
            startTime = "N/A"
            endTime = "N/A"
            location = ""
            examType = "N/A"
        } else {
            days = rowElements[7]
            startTime = if (arr) "(ARR)" else rowElements[8]
            endTime = if (arr) "(ARR)" else rowElements[9]
            location = rowElements[if (arr) 9 else 10]
            examType = rowElements[if (arr) 10 else 11]
        }

        var additionalTimes: Section.MeetingTime? = null
        var comments: String? = null

        for (it in additionalData) {
            val elements = it.getElementsByTag("td").map { it.text().replace("\n", "") }
            if (elements.first().contains("Comments for CRN")) {
                comments = elements[1]
            } else if (elements.contains("* Additional Times *")) {
                additionalTimes = Section.MeetingTime(elements[5], elements[6], elements[7], elements[8])
            } else {
                break
            }
        }

        return Section(
                crn,
                subjectCode,
                courseNumber,
                name,
                term,
                campus,
                sectionType,
                credits,
                capacity,
                instructor,
                Section.MeetingTime(days, startTime, endTime, location),
                examType,
                additionalTimes,
                comments
        )
    }

    private fun getDefaultRequestParams() = mutableMapOf<String, Any>(
            "BTN_PRESSED" to "FIND class sections",
            "SCHDTYPE" to "%",
            "disp_comments_in" to "Y"
    )
}