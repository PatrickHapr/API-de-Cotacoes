package com.example.conversordemoeda

import com.google.gson.annotations.SerializedName

data class CotacaoResponse(
    @SerializedName("bid") val bid: String
)