package cm.project.android.projectx.db.entities

data class User(
    val id: String,
    val username: String,
    val pictureUrl: String,
    val totalXP : Int,
    val addedPOIs: Int,
    val receivedRatings: Int,
    val givenRatings: Int,
){
    constructor(): this("","","",0,0,0,0)
}
