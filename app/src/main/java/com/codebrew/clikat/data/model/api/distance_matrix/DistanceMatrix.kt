package com.codebrew.clikat.data.model.api.distance_matrix


import androidx.annotation.Keep

@Keep
data class DistanceMatrix(
    val destination_addresses: List<String?>?,
    val origin_addresses: List<String?>?,
    val rows: List<Row?>?,
    val status: String?
)