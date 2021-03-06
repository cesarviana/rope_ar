package com.rope.ropelandia.log

import android.content.Context
import androidx.room.*
import java.util.*

@Entity
data class Log (
    @PrimaryKey(autoGenerate = true) val id: Int? = null,
    val message: String?,
    val time: String?
)

@Dao
interface LogDao {
    @Insert
    fun insertAll(vararg log: Log)

    @Query("SELECT * FROM log")
    fun getAll(): List<Log>
}

@Database(entities = [Log::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun logDao(): LogDao
}

class Logger(context: Context) {

    private lateinit var db: AppDatabase

    init {
        try {
            //context.deleteDatabase("rope_database")
            db = Room.databaseBuilder(
                context,
                AppDatabase::class.java,
                "rope_database"
            ).build()
        } catch (e: Exception) {
            print(e.message)
        }
    }

    fun log(tag: String, text: String) {
        if(text.contains(":")){
            val time = Date().toString()
            val logText = "$tag - ${text},${time}"
            val log = Log(message = logText, time = time)
            db.logDao().insertAll(log)
        }
    }

}