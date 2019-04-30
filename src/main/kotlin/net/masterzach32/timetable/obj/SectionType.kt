package net.masterzach32.timetable.obj

/**
 * @author Zach Kozar
 * @version 8/7/2018
 */
enum class SectionType(val id: String, val prettyName: String) {
    INDEPENDENT_STUDY("I", "Study"),
    LECTURE("L", "Lecture"),
    LAB("B", "Lab"),
    RECITATION("C", "Recitation"),
    RESEARCH("R", "Research"),
    ONLINE("ONLINE COURSE", "Online"),
    UNKNOWN("", "");

    override fun toString() = prettyName

    companion object {
        fun forId(id: String) = values().firstOrNull { it.id == id } ?: if (id.contains("L")) LECTURE else UNKNOWN
    }
}