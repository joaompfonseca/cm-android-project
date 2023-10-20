package cm.project.android.projectx.network.entities

import kotlinx.serialization.Serializable

@Serializable
data class PositionDto (
    val lat: Double,
    val lng: Double
)