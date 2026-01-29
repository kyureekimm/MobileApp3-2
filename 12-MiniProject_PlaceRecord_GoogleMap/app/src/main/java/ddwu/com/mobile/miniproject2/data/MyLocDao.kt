package ddwu.com.mobile.miniproject2.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

/*DAO 구현*/
//MyLocDao
//강의자료 02주차 p.g.11
//함수명 등을 적절히 구성
@Dao
interface MyLocDao {
    @Query("SELECT * FROM myloc_table")
    fun getAllLocs(): List<MyLoc>

    @Query("SELECT * FROM myloc_table WHERE _id = :id")
    fun getLocById(id: Int): MyLoc

    @Insert
    fun insertLoc(loc: MyLoc)

    @Update
    fun updateLoc(loc: MyLoc)

    @Delete
    fun deleteLoc(loc: MyLoc)

}
