package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "clipboard_entries")
data class ClipboardEntry(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val text: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "shortcut_phrases")
data class ShortcutPhrase(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val shortcut: String,
    val expandedText: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "typing_insights")
data class TypingInsight(
    @PrimaryKey val key: String,
    val count: Int = 0
)
