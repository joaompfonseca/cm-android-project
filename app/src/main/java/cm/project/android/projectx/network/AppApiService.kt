package cm.project.android.projectx.network

import cm.project.android.projectx.dto.heredata
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import retrofit2.Retrofit
import retrofit2.http.GET
import retrofit2.http.Query
import java.util.Objects

private const val URL_GEO = "https://geocode.search.hereapi.com/v1/"

private val retrofit = Retrofit.Builder()
    .addConverterFactory(Json.asConverterFactory("application/json".toMediaType()))
    .baseUrl(URL_GEO)
    .build()

interface AppApiService {
    @GET("geocode")
    suspend fun getLatLng(@Query("q") q: String, @Query("in") i: String, @Query("apiKey") apikey: String): HashMap<String, List<heredata>>
}

object AppApi {
    val retrofitService: AppApiService by lazy {
        retrofit.create(AppApiService::class.java)
    }
}