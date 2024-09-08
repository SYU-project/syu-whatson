package com.example.whatson

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.example.whatson.databinding.ActivitySplashBinding

class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.lottieAnimationView.apply {
            setAnimation(R.raw.animation)  // Lottie 애니메이션 파일 설정
            repeatCount = 0  // 한 번만 재생
            playAnimation()  // 애니메이션 재생
        }

        // 3초 정도 딜레이를 추가하여 스플래시 화면 유지
        Handler(Looper.getMainLooper()).postDelayed({
            startActivity(Intent(this@SplashActivity, MainActivity::class.java))
            finish()
        }, 3000)  // 3000 밀리초 = 3초
    }
}