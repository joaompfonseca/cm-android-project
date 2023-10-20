package cm.project.android.projectx.db.entities

data class POI(
    val name: String,
    val description: String,
    val type: String,
    val pictureUrl: String,
    val latitude: Double,
    val longitude: Double,
    val createdBy: String,
    val ratings: MutableList<Rating>
) {
    constructor(): this("", "", "", "", 0.0, 0.0, "", mutableListOf())
    override fun hashCode(): Int {
        return type.hashCode() + latitude.hashCode() + longitude.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        return super.equals(other)
    }
}
