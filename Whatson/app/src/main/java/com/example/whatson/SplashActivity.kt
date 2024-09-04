package com.example.whatson

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.whatson.databinding.ActivitySplashBinding

class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // GIF 로드
        Glide.with(this)
            .asGif()
            .load(R.drawable.zipup_splash) // drawable 폴더에 저장된 gif 파일
            .into(binding.gifImageView)

        // 스플래시 화면을 3초 동안 보여주고 MainActivity로 전환
        Handler(Looper.getMainLooper()).postDelayed({
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish() // SplashActivity를 종료
        }, 3000) // 3000ms = 3초
    }
}
