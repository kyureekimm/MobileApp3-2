package ddwu.com.mobile.naverretrofittest.network

import ddwu.com.mobile.naverretrofittest.data.Root
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface IBookSearchService {

    @GET("v1/search/book.json")
    suspend fun getBookList (
        @Query("query") query: String,
        @Header("X-Naver-Client-Id") clientID: String,
        @Header("X-Naver-Client-Secret") clientSecret: String,
    ) : Root

}