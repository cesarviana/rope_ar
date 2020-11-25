package com.rope.ropelandia.log

import android.content.Context
import android.os.Environment
import androidx.room.*
import java.io.File
import java.time.Instant
import java.util.*

@Entity(tableName = "log")
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

@Database(entities = [Log::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun logDao(): LogDao
}

class Logger(context: Context) {

    private lateinit var db: AppDatabase

    init {
        try {
            db = Room.databaseBuilder(
                context,
                AppDatabase::class.java,
                "rope_database"
            ).build()
        } catch (e: Exception) {
            print(e.message)
        }
    }

    // command examples: <add:l>
    private val regex = """<(\w+):(\w)>\r\n*""".toRegex()

    fun log(tag: String, text: String) {
        if (regex.matches(text)) {
            val result = regex.find(text)
            result?.let {
                it.groups[2]
            }?.let { group ->
                val command = group.value
                val time = Date().toString()
                val logText = "$tag - ${command},${time}"
                val log = Log(message = logText, time = time)
                db.logDao().insertAll(log)
            }
        }
    }

}