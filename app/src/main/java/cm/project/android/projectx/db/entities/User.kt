package cm.project.android.projectx.db.entities

data class User(
    val id: String,
    val displayName: String,
    val username: String,
    val pictureUrl: String,
    var totalXP : Int,
    var addedPOIs: Int,
    var receivedRatings: Int,
    var givenRatings: Int,
){
    constructor(): this("","","","",0,0,0,0)
}
