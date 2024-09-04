package com.example.whatson

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.airbnb.lottie.LottieAnimationView
import com.airbnb.lottie.LottieDrawable
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

            // 애니메이션이 끝난 후 메인 액티비티로 이동
            addAnimatorUpdateListener {
                if (!isAnimating) {
                    startActivity(Intent(this@SplashActivity, MainActivity::class.java))
                    finish()
                }
            }
        }
    }
}
