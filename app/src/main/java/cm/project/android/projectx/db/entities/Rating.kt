package cm.project.android.projectx.db.entities

data class Rating(
    val user: String,
    val value: Boolean
) {
    constructor(): this("", false)
}
