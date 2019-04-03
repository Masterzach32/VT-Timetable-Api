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
enum class Session(val id: String, val prettyName: String) {
    SPRING("01", "Spring"),
    SUMMER_I("06", "Summer I"),
    SUMMER_II("07", "Summer II"),
    FALL("09", "Fall"),
    WINTER("12", "Winter");

    companion object {
        fun forMonth(month: String): Session {
            return when (month.toInt()) {
                in 0..3 -> SPRING
                in 4..5 -> SUMMER_I
                6 -> SUMMER_II
                in 7..10 -> FALL
                else -> WINTER
            }
        }

        fun forCode(month: String): Session {
            return when(month.toInt()) {
                1 -> SPRING
                6 -> SUMMER_I
                7 -> SUMMER_II
                9 -> FALL
                12 -> WINTER
                else -> throw IllegalArgumentException("Code does not match session: $month")
            }
        }
    }
}