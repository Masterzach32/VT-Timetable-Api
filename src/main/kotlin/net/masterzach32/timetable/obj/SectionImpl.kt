package net.masterzach32.timetable.obj

internal data class SectionImpl(
    override val crn: String,
    override val term: Term,
    override val subjectCode: String,
    override val courseNumber: String,
    override val title: String,
    override val campus: Campus,
    override val type: SectionType,
    override val credits: String,
    override val capacity: Int,
    override val instructor: String,
    override val examType: String,
    override val meetingTimes: List<MeetingTime>,
    override val comments: String?
) : Section
