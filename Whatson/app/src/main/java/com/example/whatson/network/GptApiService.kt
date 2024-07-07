package com.example.whatson.network

import com.example.whatson.model.GptRequest
import com.example.whatson.model.GptResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface GptApiService {
    @Headers(
        "Content-Type: application/json",
        "Authorization: Bearer APIKEY"
    )
    @POST("chat/completions")
    fun complete(
        @Body request: GptRequest
    ): Call<GptResponse>
}
