package cm.project.android.projectx.db.repositories

import cm.project.android.projectx.db.entities.POI
import cm.project.android.projectx.db.entities.Rating
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await

private const val BASE_URL =
    "https://cm-android-project-39eba-default-rtdb.europe-west1.firebasedatabase.app/"


class POIRepository {

    private val db = Firebase.database(BASE_URL).getReference("poi")

    suspend fun initDB() {
        val poi1 = POI(
            name = "UA Psychology Department Parking",
            description = "Covered free parking for bicycles",
            type = "parking",
            pictureUrl = "",
            latitude = 40.63195,
            longitude = -8.65799,
            createdBy = "admin",
            ratings = mutableListOf(
                Rating("admin", true),
                Rating("user", true)
            )
        )
        val poi2 = POI(
            name = "UA Environmental Department Parking",
            description = "Free parking for bicycles",
            type = "parking",
            pictureUrl = "",
            latitude = 40.63265,
            longitude = -8.65881,
            createdBy = "admin",
            ratings = mutableListOf(
                Rating("admin", false),
                Rating("user", false)
            )
        )
        val poi3 = POI(
            name = "UA Catacumbas Bathroom",
            description = "Free bathroom",
            type = "bathroom",
            pictureUrl = "",
            latitude = 40.63071,
            longitude = -8.65875,
            createdBy = "user",
            ratings = mutableListOf(
                Rating("admin", false),
                Rating("user", true)
            )
        )
        savePOI(poi1)
        savePOI(poi2)
        savePOI(poi3)
    }

    suspend fun getAllPOIs(): List<POI> {
        val res = mutableListOf<POI>()
        val data = db.get().await()
        for (obj in data.children) {
            obj.getValue(POI::class.java)?.let { poi ->
                res.add(poi)
            }
        }
        return res
    }

    suspend fun savePOI(poi: POI) {
        val id = poi.hashCode().toString()
        db.child(id).setValue(poi).await()
    }
}