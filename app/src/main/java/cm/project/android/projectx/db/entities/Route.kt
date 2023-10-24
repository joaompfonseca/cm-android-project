package cm.project.android.projectx.db.entities

data class Route(
    val origin: String,
    val destination: String,
    val averageSpeed: Double,
    val points: MutableList<Point>,
    val createdBy: String
) {
    constructor(): this("", "", 0.0,mutableListOf(),"")
}
