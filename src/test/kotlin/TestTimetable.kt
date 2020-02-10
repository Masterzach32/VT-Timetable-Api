import junit.framework.TestCase
import net.masterzach32.timetable.Timetable
import net.masterzach32.timetable.obj.Campus
import net.masterzach32.timetable.obj.Curriculum
import net.masterzach32.timetable.obj.Session
import net.masterzach32.timetable.obj.Term
import net.masterzach32.timetable.pullSectionComments

class TestTimetable : TestCase() {

    fun testGetTerms() {
        assertEquals(
                Timetable.getAvailableTerms(),
                listOf("202001").map { Term(it) }
        )

        Timetable.lookup(subjectCode = "ESM", term = Term("202001")).forEach { println(it) }
        Timetable.lookupCrn("14709", term = Term("202001"))!!.pullSectionComments()
                .also { println(it) }
    }

//    fun testCurrentTerm() {
//        assertEquals(
//                Timetable.getCurrentTerm(),
//                Term("201901")
//        )
//    }
//
//    fun testGetAvailableSubjects() {
//        assertEquals(
//                Timetable.getAllSubjects(Term("201909")),
//                listOf("AAEC", "ACIS", "AFST", "AHRM", "AINS", "ALCE", "ALS", "AOE", "APS", "APSC", "ARBC", "ARCH", "ART", "AS", "ASPT", "AT", "BC", "BCHM", "BDS", "BIOL", "BIT", "BMES", "BMSP", "BMVS", "BSE", "BTDM", "C21S", "CAUS", "CEE", "CEM", "CHE", "CHEM", "CHN", "CINE", "CLA", "CMDA", "CNST", "COMM", "CONS", "COS", "CRIM", "CS", "CSES", "DASC", "ECE", "ECON", "EDCI", "EDCO", "EDCT", "EDEL", "EDEP", "EDHE", "EDIT", "EDP", "EDRE", "ENGE", "ENGL", "ENGR", "ENSC", "ENT", "ESM", "FA", "FCS", "FIN", "FIW", "FL", "FMD", "FR", "FREC", "FST", "GBCB", "GEOG", "GEOS", "GER", "GIA", "GR", "GRAD", "HD", "HEB", "HIST", "HNFE", "HORT", "HTM", "HUM", "IDS", "IS", "ISC", "ISE", "ITAL", "ITDS", "JPN", "JUD", "LAHS", "LAR", "LAT", "LDRS", "MACR", "MATH", "ME", "MED", "MGT", "MINE", "MKTG", "MN", "MS", "MSE", "MTRG", "MUS", "NANO", "NEUR", "NR", "NSEG", "PAPA", "PHIL", "PHS", "PHYS", "PM", "PORT", "PPWS", "PSCI", "PSVP", "PSYC", "REAL", "RED", "RLCL", "RTM", "RUS", "SBIO", "SOC", "SPAN", "SPIA", "STAT", "STL", "STS", "SYSB", "TA", "TBMH", "UAP", "UH", "UNIV", "VM", "WATR", "WGS")
//        )
//    }
//
//    fun testGetCrn() {
//        assertEquals(
//                Timetable.lookupCrn("84478", term = Term("201909"))?.courseNumber,
//                "3124"
//        )
//    }
//
//    fun testGetCourse() {
//        assertEquals(
//                Timetable.lookupCourse(subjectCode = "ESM", courseNumber = "3124", term = Term("201909")).firstOrNull()?.name,
//                "Dyn II Analytical & 3-D Motion"
//        )
//    }
//
//    fun testGetComments() {
//        assertNotNull(Timetable.lookupCrn("85942", term = Term("201909"))!!.pullSectionComments())
//    }
//
//    fun testSectionsForCle() {
//        assert(Timetable.lookupCle(Curriculum.CLE.AREA_2, Term("201909")).isNotEmpty())
//    }
}