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
private const val REV_GEOCODE_URL = "https://revgeocode.search.hereapi.com/v1/"

val json = Json { ignoreUnknownKeys = true }

private val geocode = Retrofit.Builder()
    .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
    .baseUrl(GEOCODE_URL)
    .build()

private val rev_geocode = Retrofit.Builder()
    .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
    .baseUrl(REV_GEOCODE_URL)
    .build()

interface GeocodeService {
    @GET("geocode?apiKey=${HERE_API_KEY}&in=countryCode:PRT")
    suspend fun getGeocode(@Query("q") query: String): HashMap<String, List<GeocodeDto>>
}

interface RevGeocodeService {
    @GET("revgeocode?apiKey=${HERE_API_KEY}")
    suspend fun getRevGeocode(@Query("at") query: String): HashMap<String, List<GeocodeDto>>
}

object AppApi {
    val geocodeService: GeocodeService by lazy {
        geocode.create(GeocodeService::class.java)
    }

    val revGeocodeService: RevGeocodeService by lazy {
        rev_geocode.create(RevGeocodeService::class.java)
    }
}