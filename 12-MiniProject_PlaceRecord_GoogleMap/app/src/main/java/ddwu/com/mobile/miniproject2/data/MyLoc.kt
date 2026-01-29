package ddwu.com.mobile.miniproject2.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/*Entity 구현*/
@Entity(tableName = "myloc_table")
data class MyLoc(
    @PrimaryKey(autoGenerate = true) val _id: Int,  // = 0 을 넣어주면 insert 할 때 id 입력을 생략 가능합니다.

    @ColumnInfo(name = "locTitle") var locTitle: String?,

    @ColumnInfo(name = "locAddress") var locAddress: String?,

    @ColumnInfo(name = "locMemo") var locMemo: String?,

    @ColumnInfo(name = "locLat") // 위도 (Latitude) -> locLat으로 수정
    var locLat: Double?,

    @ColumnInfo(name = "locLng") // 경도 (Longitude)
    var locLng: Double?
)
