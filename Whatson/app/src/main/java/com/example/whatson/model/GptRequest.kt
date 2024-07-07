package com.example.whatson.model

data class GptRequest(
    val prompt: String,
    val max_tokens: Int,
    val model: String
)

data class GptResponse(
    val choices: List<GptChoice>
)

data class GptChoice(
    val text: String,
    val index: Int
)
