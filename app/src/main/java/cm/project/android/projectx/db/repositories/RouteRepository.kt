package cm.project.android.projectx.db.repositories

import android.net.Uri
import cm.project.android.projectx.db.entities.POI
import cm.project.android.projectx.db.entities.Rating
import cm.project.android.projectx.db.entities.Route
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.tasks.await

private const val DB_URL =
    "https://cm-android-project-39eba-default-rtdb.europe-west1.firebasedatabase.app/"
private const val STORAGE_URL = "gs://cm-android-project-39eba.appspot.com"

class RouteRepository {

    private val db = Firebase.database(DB_URL).getReference("route")

    suspend fun getAllRoutes(): HashMap<String, List<Route>> {
        val res = HashMap<String, List<Route>>()
        val data = db.get().await()
        for (obj in data.children) {
            val uid = obj.key ?: continue
            val routes  = getAllRoutes(uid)
            res[uid] = routes
        }
        return res
    }

    suspend fun getAllRoutes(uid: String): List<Route> {
        val res = mutableListOf<Route>()
        val data = db.child(uid).get().await()
        for (obj in data.children) {
            obj.getValue(Route::class.java)?.let { route ->
                res.add(route)
            }
        }
        return res
    }

    suspend fun saveRoute(uid: String, route: Route) {
        val id = route.hashCode().toString()
        db.child(uid).child(id).setValue(route).await()
    }
}