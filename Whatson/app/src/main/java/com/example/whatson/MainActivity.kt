package com.example.whatson

import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.whatson.databinding.ActivityMainBinding
import kotlin.random.Random

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter: ArticleAdapter
    private val articles = mutableListOf<Article>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 테스트용으로 데이터 랜덤 생성
        generateRandomArticles(50)

        adapter = ArticleAdapter(articles)
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter

        binding.swipeRefreshLayout.setOnRefreshListener {
            // 새로고침 시 데이터를 갱신합니다.
            Handler().postDelayed({
                articles.shuffle()
                adapter.notifyDataSetChanged()
                binding.swipeRefreshLayout.isRefreshing = false
            }, 2000) // 2초 후에 새로고침을 완료합니다.
        }
    }

    private fun generateRandomArticles(count: Int, addToFront: Boolean = false) {
        val newArticles = mutableListOf<Article>()
        for (i in 1..count) {
            val title = "Title ${Random.nextInt(1000)}"
            val content = "Content ${Random.nextInt(1000)}"
            newArticles.add(Article(title, content))
        }
        if (addToFront) {
            articles.addAll(0, newArticles)
        } else {
            articles.addAll(newArticles)
        }
        articles.shuffle() // 목록을 랜덤하게 섞습니다.
    }
}
