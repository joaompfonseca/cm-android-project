package cm.project.android.projectx.dto

import kotlinx.serialization.Serializable

@Serializable()
data class scoring(
    val queryScore: Double,
    val fieldScore: HashMap<String, Double>
)
