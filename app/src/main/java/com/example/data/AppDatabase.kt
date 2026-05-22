package com.example.data

import android.content.Context
import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ClipboardDao {
    @Query("SELECT * FROM clipboard_entries ORDER BY timestamp DESC")
    fun getAll(): Flow<List<ClipboardEntry>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: ClipboardEntry)

    @Query("DELETE FROM clipboard_entries WHERE id = :id")
    suspend fun delete(id: Int)

    @Query("DELETE FROM clipboard_entries")
    suspend fun clearAll()
}

@Dao
interface ShortcutDao {
    @Query("SELECT * FROM shortcut_phrases ORDER BY timestamp DESC")
    fun getAll(): Flow<List<ShortcutPhrase>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(shortcut: ShortcutPhrase)

    @Query("DELETE FROM shortcut_phrases WHERE id = :id")
    suspend fun delete(id: Int)
}

@Dao
interface InsightDao {
    @Query("SELECT * FROM typing_insights")
    fun getAll(): Flow<List<TypingInsight>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(insight: TypingInsight)

    @Query("SELECT * FROM typing_insights WHERE `key` = :key LIMIT 1")
    suspend fun getByKey(key: String): TypingInsight?

    @Transaction
    suspend fun incrementInsight(key: String, byAmount: Int = 1) {
        val current = getByKey(key)
        val newCount = (current?.count ?: 0) + byAmount
        insert(TypingInsight(key, newCount))
    }
}

@Database(entities = [ClipboardEntry::class, ShortcutPhrase::class, TypingInsight::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun clipboardDao(): ClipboardDao
    abstract fun shortcutDao(): ShortcutDao
    abstract fun insightDao(): InsightDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "keyassist_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
