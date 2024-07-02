package com.example.whatson

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.whatson.components.TitleBodyComponent

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val component1 = findViewById<TitleBodyComponent>(R.id.component1)
        val component2 = findViewById<TitleBodyComponent>(R.id.component2)
        val component3 = findViewById<TitleBodyComponent>(R.id.component3)

        component1.setTitle("Title 1")
        component1.setBody("This is the body text for the first component.")

        component2.setTitle("Title 2")
        component2.setBody("This is the body text for the second component.")

        component3.setTitle("Title 3")
        component3.setBody("This is the body text for the third component.")
    }
}
