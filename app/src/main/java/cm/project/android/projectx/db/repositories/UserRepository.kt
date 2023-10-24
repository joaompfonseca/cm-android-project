package cm.project.android.projectx.db.repositories

import android.net.Uri
import android.util.Log
import cm.project.android.projectx.db.entities.User
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.tasks.await

private const val DB_URL =
    "https://cm-android-project-39eba-default-rtdb.europe-west1.firebasedatabase.app/"
private const val STORAGE_URL = "gs://cm-android-project-39eba.appspot.com"

class UserRepository {

    private val db = Firebase.database(DB_URL).getReference("user")
    private val storage = Firebase.storage(STORAGE_URL).getReference("user")

    suspend fun getUser(id: String) : User? {
        val user = db.child(id).get().await().getValue(User::class.java)
        return user
    }

    suspend fun saveUser(user: User, imageUri: Uri): User {
        val id = user.id
        storage.child("$id.jpg").putFile(imageUri).await()
        val updatedUser = user.copy(pictureUrl = storage.child("$id.jpg").downloadUrl.await().toString())
        db.child(id).setValue(updatedUser).await()
        return updatedUser
    }
}