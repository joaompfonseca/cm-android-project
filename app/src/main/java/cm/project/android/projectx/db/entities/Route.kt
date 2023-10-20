package cm.project.android.projectx.db.entities

data class Route(
    val origin: String,
    val destination: String,
    val createdBy: String
) {
    constructor(): this("", "", "")
}
