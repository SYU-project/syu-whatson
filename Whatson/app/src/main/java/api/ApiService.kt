package com.example.whatson.api

import retrofit2.Call
import retrofit2.http.GET

interface ApiService {
    @GET("/summaries")
    fun getSummaries(): Call<List<Summary>>
}
