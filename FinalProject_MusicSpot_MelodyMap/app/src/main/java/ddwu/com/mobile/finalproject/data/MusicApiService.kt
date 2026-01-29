package ddwu.com.mobile.finalproject.data

import retrofit2.http.GET
import retrofit2.http.Query


interface MusicApiService {

    @GET("search")
    suspend fun searchSongs(
        @Query("term") term: String,
        @Query("entity") entity: String = "song"
    ): MusicResponse
}