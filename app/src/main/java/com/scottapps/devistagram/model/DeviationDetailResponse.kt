package com.scottapps.devistagram.model

import com.google.gson.annotations.SerializedName

data class DeviationDetailResponse(
    @SerializedName("deviation")
    val deviation: Deviation?
)
