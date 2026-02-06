package ddwu.com.mobile.miniproject3.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

/*Memo Entity 구현*/

@Entity("memo_table")
data class Memo (

    @PrimaryKey(autoGenerate = true) val _id: Long,
    //@ColumnInfo(name = "memo_title") 이게 다 필요가 없네??
    var title: String?,
    @ColumnInfo(name = "memo_contents") var contents : String?,
    @ColumnInfo(name = "memo_path") var imagePath: String
) : Serializable // intent에 넣을때 ?