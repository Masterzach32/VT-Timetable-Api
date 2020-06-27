package net.masterzach32.timetable.obj

interface Section {

    /**
     * The section's CRN (Course Request Number or Course Reference Number), between one and five digits.
     */
    val crn: String

    /**
     * The corresponding term.
     */
    val term: Term

    /**
     * Subject of the section, usually the department code.
     */
    val subjectCode: String

    /**
     * Course number.
     */
    val courseNumber: String

    /**
     * Title of the course.
     */
    val title: String

    /**
     * Campus the section is located on.
     */
    val campus: Campus

    /**
     * Type of class (lecture, seminar, lab...).
     */
    val type: SectionType

    /**
     * Number of credits for completing the class.
     */
    val credits: String

    /**
     * Total number of seats in the section.
     */
    val capacity: Int

    /**
     * Instructor that is teaching this section.
     */
    val instructor: String

    /**
     * The exam type code
     */
    val examType: String

    /**
     * Days/Times/Locations the section meets.
     */
    val meetingTimes: List<MeetingTime>

    /**
     * Any inline comments on the timetable.
     */
    val comments: String?
}