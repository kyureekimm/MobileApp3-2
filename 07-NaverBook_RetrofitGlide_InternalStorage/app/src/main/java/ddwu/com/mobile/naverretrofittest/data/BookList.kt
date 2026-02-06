package ddwu.com.mobile.naverretrofittest.data

import java.io.Serializable

data class Root(
    val lastBuildDate: String,
    val total: Long,
    val start: Long,
    val display: Long,
    val items: List<Book>,
)

data class Book(
    val title: String,
    val link: String,
    val image: String,
    val author: String,
    val discount: String,
    val publisher: String,
    val pubdate: String,
    val isbn: String,
    val description: String,
): Serializable {
   override fun toString()= "$title ($author)"
}