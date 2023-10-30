package cm.project.android.projectx.db.entities

data class Route(
    val origin: String,
    val destination: String,
    val totalDistance: Double,
    val totalDuration: Long,
    val points: MutableList<Point>,
    val createdBy: String
) {
    constructor(): this("", "", 0.0,0L,mutableListOf(),"")
    override fun hashCode(): Int {
        return points.first().timestamp.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        return super.equals(other)
    }
}
