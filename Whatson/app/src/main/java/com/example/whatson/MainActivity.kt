import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.whatson.databinding.ActivityMainBinding
import com.example.whatson.databinding.ItemArticleBinding
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter: ArticleAdapter
    private val articles = mutableListOf<Article>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        adapter = ArticleAdapter(articles)
        binding.recyclerView.adapter = adapter
        binding.recyclerView.layoutManager = LinearLayoutManager(this)

        // 임시로 기사 데이터를 추가
        addDummyArticles()

        // SwipeRefreshLayout 설정
        binding.swipeRefreshLayout.setOnRefreshListener {
            // 새로고침 동작 처리
            refreshArticles()
            binding.swipeRefreshLayout.isRefreshing = false
        }
    }

    private fun addDummyArticles() {
        // 임의의 기사 데이터 추가
        val dummyData = mutableListOf<Article>()
        for (i in 1..20) {
            dummyData.add(Article("제목 $i", "내용 $i"))
        }
        articles.addAll(dummyData)
        adapter.notifyDataSetChanged()
    }

    private fun refreshArticles() {
        // 여기에서 새로고침 로직을 구현할 수 있습니다.
        // 예를 들어 새로운 데이터를 서버에서 가져오거나, 랜덤하게 데이터를 바꾸는 등의 작업을 수행할 수 있습니다.
        // 이 예제에서는 임의의 데이터를 다시 추가하는 방식으로 구현했습니다.
        addDummyArticles()
    }
}
