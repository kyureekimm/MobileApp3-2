package ddwu.com.mobile.finalproject.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface MusicSpotDao {
    @Insert
    suspend fun insertSpot(spot: MusicSpot)

    @Update
    suspend fun updateSpot(spot: MusicSpot)

    @Delete
    suspend fun deleteSpot(spot: MusicSpot)


    //가장 최근에 쓴 글이 맨 위ㅐ
    @Query("SELECT * FROM music_spot_table ORDER BY id DESC") //역순
    fun getAllSpots(): Flow<List<MusicSpot>>

    @Query("SELECT * FROM music_spot_table WHERE id = :id")
    fun getSpotById(id: Int): Flow<MusicSpot?>

}