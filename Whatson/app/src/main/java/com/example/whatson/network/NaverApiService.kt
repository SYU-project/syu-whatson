package com.example.whatson.network

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query
import com.example.whatson.model.NaverSearchResponse

interface NaverApiService {
    @GET("/v1/search/news.json")
    fun searchNews(
        @Header("X-Naver-Client-Id") clientId: String,
        @Header("X-Naver-Client-Secret") clientSecret: String,
        @Query("query") query: String
    ): Call<NaverSearchResponse>
}
