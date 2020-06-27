package net.masterzach32.timetable.obj

/**
 * @author Zach Kozar
 * @version 8/7/2018
 */
sealed class Curriculum(val code: String) {

    object All : Curriculum("AR%")

    sealed class CLE(val desc: String, id: String) : Curriculum("AR$id") {
        object Area1 : CLE("Writing and Discourse", "01")
        object Area2 : CLE("Ideas, Cultural Traditions and Values", "02")
        object Area3 : CLE("Society and Human Behavior", "03")
        object Area4 : CLE("Scientific Reasoning and Discovery", "04")
        object Area5 : CLE("Quantitative and Symbolic Reasoning", "05")
        object Area6 : CLE("Creativity and Aesthetic Experience", "06")
        object Area7 : CLE("Critical Issues in a Global Context", "07")
    }

    sealed class Pathway(val desc: String, id: String) : Curriculum("G0$id") {
        object Pathway1A : Pathway("Advanced/Applied Discourse", "1A")
        object Pathway1F : Pathway("Foundational Discourse", "1F")
        object Pathway2 : Pathway("Critical Thinking in the Humanities", "2")
        object Pathway3 : Pathway("Reasoning in the Social Sciences", "3")
        object Pathway4 : Pathway("Reasoning in the Natural Sciences", "4")
        object Pathway5A : Pathway("Advanced/Applied Quantitative and Computational Thinking", "5A")
        object Pathway5F : Pathway("Foundational Quantitative and Computational Thinking", "5F")
        object Pathway6A : Pathway("Critique and Practice in the Arts", "6A")
        object Pathway6D : Pathway("Critique and Practice in Design", "6D")
        object Pathway7 : Pathway("Critical Analysis of Equity and Identity in the United States", "7")
    }
}