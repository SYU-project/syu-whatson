import android.view.*
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.whatson.R

class ArticleAdapter(private val articles: MutableList<Article>) :
    RecyclerView.Adapter<ArticleAdapter.ArticleViewHolder>() {

    // ViewHolder 클래스 정의
    class ArticleViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val titleTextView: TextView = itemView.findViewById(R.id.text_title)
        val contentTextView: TextView = itemView.findViewById(R.id.text_content)
    }

    // onCreateViewHolder: ViewHolder 객체 생성
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArticleViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_article, parent, false)
        return ArticleViewHolder(view)
    }

    // onBindViewHolder: 데이터를 ViewHolder에 바인딩
    override fun onBindViewHolder(holder: ArticleViewHolder, position: Int) {
        val article = articles[position]
        holder.titleTextView.text = article.title
        holder.contentTextView.text = article.content
    }

    // getItemCount: 데이터 아이템의 개수 반환
    override fun getItemCount(): Int {
        return articles.size
    }

    // updateData: 데이터 갱신을 위한 메서드
    fun updateData(newArticles: List<Article>) {
        articles.clear()
        articles.addAll(newArticles)
        notifyDataSetChanged()
    }
}
