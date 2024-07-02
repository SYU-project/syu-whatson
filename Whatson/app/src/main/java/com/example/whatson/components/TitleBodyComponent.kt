package com.example.whatson.components

import android.content.Context
import android.util.AttributeSet
import android.widget.LinearLayout
import android.widget.TextView
import com.example.whatson.R

class TitleBodyComponent @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private var titleTextView: TextView
    private var bodyTextView: TextView

    init {
        inflate(context, R.layout.component_title_body, this)
        titleTextView = findViewById(R.id.titleTextView)
        bodyTextView = findViewById(R.id.bodyTextView)
    }

    fun setTitle(title: String) {
        titleTextView.text = title
    }

    fun setBody(body: String) {
        bodyTextView.text = body
    }
}
