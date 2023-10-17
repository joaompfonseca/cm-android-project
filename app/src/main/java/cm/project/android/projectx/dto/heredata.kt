package cm.project.android.projectx.dto

import kotlinx.serialization.Serializable

@Serializable()
data class heredata (
    val title: String,
    val id: String,
    val resultType: String,
    val localityType: String,
    val address: HashMap<String, String>,
    val position: HashMap<String, Double>,
    val mapView: HashMap<String, Double>,
    val scoring: scoring
)