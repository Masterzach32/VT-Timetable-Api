package net.masterzach32.timetable.obj

/**
 * @author Zach Kozar
 * @version 8/7/2018
 */
class Term(val month: Session, val year: String) {

    constructor(code: String) : this(Session.forCode(code.substring(4..5)), code.substring(0..3))

    val code = "$year${month.id}"

    override fun toString() = "${month.prettyName} $year"

    override fun equals(other: Any?): Boolean {
        if (other is Term)
            return this.code == other.code
        return false
    }

    override fun hashCode(): Int {
        var result = month.hashCode()
        result = 31 * result + year.hashCode()
        result = 31 * result + code.hashCode()
        return result
    }
}