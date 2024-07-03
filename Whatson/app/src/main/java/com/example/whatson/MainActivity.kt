package com.example.whatson

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.whatson.api.ApiService
import com.example.whatson.api.RetrofitClient
import com.example.whatson.api.Summary
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity() {
    private lateinit var textView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        textView = findViewById(R.id.textView)

        val apiService = RetrofitClient.instance.create(ApiService::class.java)
        apiService.getSummaries().enqueue(object : Callback<List<Summary>> {
            override fun onResponse(call: Call<List<Summary>>, response: Response<List<Summary>>) {
                if (response.isSuccessful) {
                    val summaries = response.body()
                    summaries?.forEach { summary ->
                        textView.append("URL: ${summary.url}\n")
                        textView.append("Summary: ${summary.summary}\n\n")
                    }
                }
            }

            override fun onFailure(call: Call<List<Summary>>, t: Throwable) {
                textView.text = "Error: ${t.message}"
            }
        })
    }
}
