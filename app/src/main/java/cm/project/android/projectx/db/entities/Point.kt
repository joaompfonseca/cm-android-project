package cm.project.android.projectx.db.entities

data class Point(
    val latitude: Double,
    val longitude: Double,
    val timestamp: Long
) {
    constructor(): this(0.0,0.0,0L)
}
