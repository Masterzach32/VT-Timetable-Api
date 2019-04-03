package net.masterzach32.timetable.obj

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