package com.example.whatson.model

import com.google.gson.annotations.SerializedName

data class NaverSearchResponse(
    @SerializedName("items") val items: List<NaverSearchItem>
)

data class NaverSearchItem(
    @SerializedName("title") val title: String,
    @SerializedName("description") val description: String
)
