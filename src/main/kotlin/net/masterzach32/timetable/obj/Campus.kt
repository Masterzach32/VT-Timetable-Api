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
enum class Campus(val id: Int, val prettyName: String) {
    BLACKSBURG(0, "Blacksburg"),
    VIRTUAL(10, "Virtual"),
    WESTERN(2, "Western"),
    VALLEY(3, "Valley"),
    NATIONAL_CAPITAL_REGION(4, "National Capital Region"),
    CENTRAL(6, "Central"),
    HAMPTON_ROADS_CENTER(7, "Hampton Roads Center"),
    CAPITAL(8, "Capital"),
    OTHER(9, "Other");

    override fun toString() = prettyName
}