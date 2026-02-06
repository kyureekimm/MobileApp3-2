package ddwu.com.mobile.miniproject3.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

/*Memo Database 구현*/

@Database([Memo::class], version = 1)
abstract class MemoDatabase : RoomDatabase() {
    abstract fun getMemoDao() : MemoDao

    companion object {  //싱글톤패턴. 매번 이걸로 쓰자는 거야?
        @Volatile private var INSTANCE : MemoDatabase? = null

        fun getInstance(context: Context) : MemoDatabase =
            INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MemoDatabase::class.java,
                    "memo_database.db").build()
                INSTANCE = instance
                instance
            }
    }
}

