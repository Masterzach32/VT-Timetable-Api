package net.masterzach32.timetable

import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.features.*
import io.ktor.client.features.cookies.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.client.statement.*
import io.ktor.http.*
import net.masterzach32.timetable.obj.*
import org.jsoup.*
import org.jsoup.nodes.*
import org.slf4j.*
import java.util.*

object Timetable {

    private val logger = LoggerFactory.getLogger(this.javaClass)
    const val GET_URL = "https://apps.es.vt.edu/ssb/HZSKVTSC.P_DispRequest"
    const val POST_URL = "https://apps.es.vt.edu/ssb/HZSKVTSC.P_ProcRequest"
    const val COMMENTS_URL = "https://apps.es.vt.edu/ssb/HZSKVTSC.P_ProcComments"
    private const val OPEN_ONLY_DEFAULT = false

    internal val httpClient = HttpClient(OkHttp) {
        BrowserUserAgent()
        install(HttpCookies)
    }

    @JvmOverloads
    suspend fun lookupCrn(
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

    @JvmOverloads
    suspend fun lookupCourse(
        subjectCode: String,
        courseNumber: String,
        curriculum: Curriculum = Curriculum.All,
        term: Term = getCurrentTerm(),
        openOnly: Boolean = OPEN_ONLY_DEFAULT
    ): List<Section> {
        return lookup(
            subjectCode = subjectCode,
            courseNumber = courseNumber,
            curriculum = curriculum,
            term = term,
            openOnly = openOnly
        )
    }

    @JvmOverloads
    suspend fun lookupCle(
        area: Curriculum,
        term: Term = getCurrentTerm(),
        openOnly: Boolean = OPEN_ONLY_DEFAULT
    ): List<Section> {
        return lookup(curriculum = area, term = term, openOnly = openOnly)
    }

    @JvmOverloads
    suspend fun lookup(
        crn: String? = null,
        subjectCode: String? = null,
        courseNumber: String? = null,
        curriculum: Curriculum = Curriculum.All,
        term: Term = getCurrentTerm(),
        campus: Campus = Campus.BLACKSBURG,
        sectionType: SectionType? = null,
        openOnly: Boolean = OPEN_ONLY_DEFAULT
    ): List<Section> {
        var params = defaultRequestParams + parametersOf(
            "CAMPUS" to listOf(campus.id.toString()),
            "TERMYEAR" to listOf(term.code),
            "CORE_CODE" to listOf(curriculum.code)
        )

        if (crn != null) {
            require(crn.length > 3) {
                "Invalid CRN: must be longer than 3 characters."
            }
            params += parametersOf("crn", crn)
        }

        if (subjectCode != null)
            params += parametersOf("subj_code", subjectCode)

        if (courseNumber != null) {
            require(courseNumber.length != 4) {
                "Invalid Class Number: must be 4 characters."
            }
            params += parametersOf("CRSE_NUMBER", courseNumber)
        }

        if (subjectCode == null && courseNumber != null)
            error("A subject code must be supplied with a class number. (ie. ENGL 1105)")

        if (sectionType != null)
            params += parametersOf("SCHDTYPE", sectionType.id)

        params += parametersOf("open_only", if (openOnly) "on" else "")

        val response: HttpResponse = httpClient.submitForm(POST_URL, params)

        return parseTable(Jsoup.parse(response.readText()), term, campus)
    }

    suspend fun getAvailableTerms(): List<Term> {
        val response: HttpResponse = httpClient.get(GET_URL)

        return Jsoup.parse(response.readText()).body()
            .getElementsByAttributeValue("name", "TERMYEAR")
            .first()
            .children()
            .drop(1)
            .filter { it.attr("value") != null }
            .distinctBy { it.attr("value") }
            .map { Term(it.attr("value")) }
    }

    fun getCurrentTerm(): Term {
        return Calendar.getInstance().run {
            Term(Session.forMonth(get(Calendar.MONTH).toString()), get(Calendar.YEAR).toString())
        }
    }

    @JvmOverloads
    suspend fun getAllSubjects(term: Term = getCurrentTerm()): List<String> {
        val params = defaultRequestParams + parametersOf(
            "CAMPUS" to listOf(Campus.BLACKSBURG.id.toString()),
            "TERMYEAR" to listOf(term.code)
        )

        val response: HttpResponse = httpClient.submitForm(POST_URL, params)

        val regex1 = "case \"${term.code}\" :.+?break;".toRegex(setOf(RegexOption.DOT_MATCHES_ALL))
        val regex2 = "\"([^\"]+)\",\"([\\w\\d&-]{2,4})\"".toRegex()

        return regex1.findAll(response.readText())
            .flatMap { regex2.findAll(it.groupValues[0]) }
            .map { it.groupValues[1] }
            .toList()
    }

    suspend fun getSectionDetails(section: Section): SectionDetails {
        return section.pullSectionDetails()
    }

    private fun parseTable(html: Document, term: Term, campus: Campus): List<Section> {
        val table = html.selectFirst(".dataentrytable") ?: return emptyList()
        val sections = mutableListOf<Section>()
        val rows = table.getElementsByTag("tr").drop(1)

        rows.indices.forEach {
            val row = rows[it]
            try {
                val additionalData = when {
                    it + 2 < rows.size -> listOf(rows[it + 1], rows[it + 2])
                    it + 1 < rows.size -> listOf(rows[it + 1])
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

        val meetingTimes: MutableList<MeetingTime> = mutableListOf(MeetingTime(days, startTime, endTime, location))

        var comments: String? = null

        for (it in additionalData) {
            val elements = it.getElementsByTag("td").map { it.text().replace("\n", "") }
            if (elements.first().contains("Comments for CRN")) {
                comments = elements[1]
            } else if (elements.contains("* Additional Times *")) {
                meetingTimes.add(MeetingTime(elements[5], elements[6], elements[7], elements[8]))
            } else {
                break
            }
        }

        return SectionImpl(
            crn,
            term,
            subjectCode,
            courseNumber,
            name,
            campus,
            sectionType,
            credits,
            capacity,
            instructor,
            examType,
            meetingTimes,
            comments
        )
    }

    val defaultRequestParams: Parameters
        get() = parametersOf(
            "BTN_PRESSED" to listOf("FIND class sections"),
            "SCHDTYPE" to listOf("%"),
            "disp_comments_in" to listOf("Y")
        )
}