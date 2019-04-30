package net.masterzach32.timetable.obj

/**
 * @author Zach Kozar
 * @version 8/7/2018
 */
interface Curriculum {

    val code: String

    object All : Curriculum {
        override val code = "AR%"
    }

    enum class CLE(val desc: String, id: String) : Curriculum {
        AREA_1("Writing and Discourse", "01"),
        AREA_2("Ideas, Cultural Traditions and Values", "02"),
        AREA_3("Society and Human Behavior", "03"),
        AREA_4("Scientific Reasoning and Discovery", "04"),
        AREA_5("Quantitative and Symbolic Reasoning", "05"),
        AREA_6("Creativity and Aesthetic Experience", "06"),
        AREA_7("Critical Issues in a Global Context", "07");

        override val code = "AR$id"
    }

    enum class Pathway(val desc: String, id: String) : Curriculum {
        PATHWAY_1A("Advanced/Applied Discourse", "1A"),
        PATHWAY_1F("Foundational Discourse", "1F"),
        PATHWAY_2("Critical Thinking in the Humanities", "2"),
        PATHWAY_3("Reasoning in the Social Sciences", "3"),
        PATHWAY_4("Reasoning in the Natural Sciences", "4"),
        PATHWAY_5A("Advanced/Applied Quantitative and Computational Thinking", "5A"),
        PATHWAY_5F("Foundational Quantitative and Computational Thinking", "5F"),
        PATHWAY_6A("Critique and Practice in the Arts", "6A"),
        PATHWAY_6D("Critique and Practice in Design", "6D"),
        PATHWAY_7("Critical Analysis of Equity and Identity in the United States", "7");

        override val code = "G0$id"
    }
}