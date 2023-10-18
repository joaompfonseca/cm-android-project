package cm.project.android.projectx.network

import cm.project.android.projectx.network.entities.GeocodeDto
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import retrofit2.Retrofit
import retrofit2.http.GET
import retrofit2.http.Query

private const val HERE_API_KEY = "HlogdPHLwPTb9a-4u-7ep-WFPOaUOZVjYx7_--mEJfw"
private const val GEOCODE_URL = "https://geocode.search.hereapi.com/v1/"

val json = Json { ignoreUnknownKeys = true }

private val geocode = Retrofit.Builder()
    .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
    .baseUrl(GEOCODE_URL)
    .build()

interface GeocodeService {
    @GET("geocode?apiKey=${HERE_API_KEY}&in=countryCode:PRT")
    suspend fun getGeocode(@Query("q") query: String): HashMap<String, List<GeocodeDto>>
}

object AppApi {
    val geocodeService: GeocodeService by lazy {
        geocode.create(GeocodeService::class.java)
    }
}