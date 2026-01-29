package ddwu.com.mobile.miniproject2.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

/*RoomDatabase 구현*/
//MyLocDatabase
//강의자료 02주차 p.g.15
//앞서 작성한 RoomEntity 와 RoomDao 에 맞게 함수 명 등 변경

@Database(entities = [MyLoc::class], version = 1)
abstract class MyLocDatabase : RoomDatabase() {
    abstract fun myLocDao() : MyLocDao

    companion object {
        @Volatile private var INSTANCE : MyLocDatabase? = null

        fun getInstance(context: Context) : MyLocDatabase =
            INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MyLocDatabase::class.java,
                    "myLoc_db").build()
                INSTANCE = instance
                instance
            }
    }
}
