package com.example.whatson

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ArticleAdapter(private val articles: List<Article>) :
    RecyclerView.Adapter<ArticleAdapter.ArticleViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArticleViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_article, parent, false)
        return ArticleViewHolder(view)
    }

    override fun onBindViewHolder(holder: ArticleViewHolder, position: Int) {
        val article = articles[position]
        holder.title.text = article.title
        holder.content.text = article.content
    }

    override fun getItemCount() = articles.size

    class ArticleViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.findViewById(R.id.article_title)
        val content: TextView = itemView.findViewById(R.id.article_content)
    }
}
