package com.example.whatson.model

data class Article(
    val title: String,
    val description: String,
    var summarizedTitle: String = "",
    var summarizedContent: String = ""
)
