package com.example.whatson

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.whatson.databinding.ActivityMainBinding
import com.example.whatson.model.Article
import com.example.whatson.model.GptRequest
import com.example.whatson.model.GptResponse
import com.example.whatson.model.NaverSearchItem
import com.example.whatson.model.NaverSearchResponse
import com.example.whatson.network.GptApiService
import com.example.whatson.network.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import okhttp3.logging.HttpLoggingInterceptor

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter: ArticleAdapter
    private val articles = mutableListOf<Article>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set API key for RetrofitClient
        RetrofitClient.setApiKey("APIKEY")

        adapter = ArticleAdapter(articles)
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter

        fetchNews()
    }

    private fun fetchNews() {
        val naverApiService = RetrofitClient.create()
        val clientId = "bgAT_xTb25hIu3uLgEXk" // 네이버 개발자 센터에서 발급받은 Client ID 입력
        val clientSecret = "rzqCK77a3Y" // 네이버 개발자 센터에서 발급받은 Client Secret 입력
        val query = "뉴스" // 검색할 키워드

        naverApiService.searchNews(clientId, clientSecret, query)
            .enqueue(object : Callback<NaverSearchResponse> {
                override fun onResponse(
                    call: Call<NaverSearchResponse>,
                    response: Response<NaverSearchResponse>
                ) {
                    if (response.isSuccessful) {
                        response.body()?.let { naverSearchResponse ->
                            val items = naverSearchResponse.items
                            summarizeArticles(items)
                        }
                    } else {
                        // API 호출은 성공했지만 응답이 실패인 경우 처리
                        Log.e("MainActivity", "Naver API response failed: $response")
                    }
                }

                override fun onFailure(call: Call<NaverSearchResponse>, t: Throwable) {
                    // API 호출 자체가 실패한 경우 처리
                    Log.e("MainActivity", "Naver API call failed", t)
                }
            })
    }

    private fun summarizeArticles(items: List<NaverSearchItem>) {
        val gptApiService = RetrofitClient.createGptApi()

        items.forEach { item ->
            // GPT API 요청 설정
            // val request = GptRequest(prompt = "요약: ${item.title}\n${item.description}", max_tokens = 50)
            val request = GptRequest(prompt = "요약: ${item.title}", max_tokens = 50, model = "gpt-3.5-turbo-0125")
            gptApiService.complete(request)
                .enqueue(object : Callback<GptResponse> {
                    override fun onResponse(call: Call<GptResponse>, response: Response<GptResponse>) {
                        if (response.isSuccessful) {
                            val gptResponse = response.body()
                            val summarizedTitle = gptResponse?.choices?.get(0)?.text?.trim() ?: "요약 제목을 가져오지 못했습니다."
                            val summarizedContent = gptResponse?.choices?.getOrNull(1)?.text?.trim() ?: "요약 내용을 가져오지 못했습니다."

                            // 새로운 Article 객체 생성
                            val summarizedArticle = Article(item.title, item.description, summarizedTitle, summarizedContent)

                            // articles 리스트에 추가
                            articles.add(summarizedArticle)

                            // RecyclerView 갱신
                            adapter.notifyDataSetChanged()
                        } else {
                            // GPT API 호출은 성공했지만 응답이 실패인 경우 처리
                            Log.e("MainActivity", "GPT API response failed: $response")
                            articles.add(Article(item.title, item.description))
                            adapter.notifyDataSetChanged()
                        }
                    }

                    override fun onFailure(call: Call<GptResponse>, t: Throwable) {
                        // GPT API 호출 자체가 실패한 경우 처리
                        Log.e("MainActivity", "GPT API call failed", t)
                        articles.add(Article(item.title, item.description))
                        adapter.notifyDataSetChanged()
                    }
                })
        }
    }
}
