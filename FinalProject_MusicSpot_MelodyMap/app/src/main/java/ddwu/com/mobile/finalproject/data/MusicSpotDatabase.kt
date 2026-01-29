package ddwu.com.mobile.finalproject.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [MusicSpot::class], version = 1)
abstract class MusicSpotDatabase : RoomDatabase() {
    abstract fun musicSpotDao(): MusicSpotDao

    companion object {
        @Volatile
        private var INSTANCE: MusicSpotDatabase? = null

        fun getInstance(context: Context): MusicSpotDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MusicSpotDatabase::class.java,
                    "music_spot_db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}