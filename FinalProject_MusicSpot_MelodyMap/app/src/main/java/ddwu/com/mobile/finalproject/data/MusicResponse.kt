package ddwu.com.mobile.finalproject.data


data class MusicResponse(
    val resultCount: Int,
    val results: List<Track>
)

// 개별 노래의 상세 정보를 담는 클래스
data class Track(
    val trackName: String?,
    val artistName: String?,
    val artworkUrl100: String?   //앨범 커버-URL
)