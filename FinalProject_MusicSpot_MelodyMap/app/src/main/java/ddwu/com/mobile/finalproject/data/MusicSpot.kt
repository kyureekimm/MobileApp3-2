package ddwu.com.mobile.finalproject.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "music_spot_table")
data class MusicSpot(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    var songTitle: String?,
    var artistName: String?,
    var albumUrl: String?,
    var photoPath: String?,
    var locLat: Double?,
    var locLng: Double?,
    var locAddress: String?,
    var memo: String?,
    val saveDate: String? //저장 날짜
) : Serializable