package cm.project.android.projectx.network.entities

import kotlinx.serialization.Serializable

@Serializable
data class GeocodeDto (
    val title: String,
    val position: PositionDto
)