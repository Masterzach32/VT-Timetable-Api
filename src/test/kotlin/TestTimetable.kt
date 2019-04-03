import junit.framework.TestCase
import net.masterzach32.timetable.Timetable
import net.masterzach32.timetable.obj.Campus
import net.masterzach32.timetable.obj.Session
import net.masterzach32.timetable.obj.Term

class TestTimetable : TestCase() {

    fun testGetTerms() {
        Timetable.getAvailableTerms()
    }
}