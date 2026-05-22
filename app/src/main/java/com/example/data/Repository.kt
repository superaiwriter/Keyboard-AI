package com.example.data

import android.content.Context
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull

class AppRepository(private val db: AppDatabase) {

    val clipboardEntries: Flow<List<ClipboardEntry>> = db.clipboardDao().getAll()
    val shortcutPhrases: Flow<List<ShortcutPhrase>> = db.shortcutDao().getAll()
    val typingInsights: Flow<List<TypingInsight>> = db.insightDao().getAll()

    suspend fun addClipboardEntry(text: String) {
        if (text.isNotBlank()) {
            db.clipboardDao().insert(ClipboardEntry(text = text))
        }
    }

    suspend fun deleteClipboardEntry(id: Int) {
        db.clipboardDao().delete(id)
    }

    suspend fun clearClipboard() {
        db.clipboardDao().clearAll()
    }

    suspend fun addShortcut(shortcut: String, expandedText: String) {
        if (shortcut.isNotBlank() && expandedText.isNotBlank()) {
            db.shortcutDao().insert(ShortcutPhrase(shortcut = shortcut.trim().lowercase(), expandedText = expandedText.trim()))
        }
    }

    suspend fun deleteShortcut(id: Int) {
        db.shortcutDao().delete(id)
    }

    suspend fun incrementInsight(key: String, byAmount: Int = 1) {
        db.insightDao().incrementInsight(key, byAmount)
    }

    // Populate initial default data if empty
    suspend fun populateDefaultsIfNeeded() {
        // Populate default shortcuts
        val existingShortcuts = db.shortcutDao().getAll().firstOrNull()
        if (existingShortcuts.isNullOrEmpty()) {
            db.shortcutDao().insert(ShortcutPhrase(shortcut = "brb", expandedText = "Be right back!"))
            db.shortcutDao().insert(ShortcutPhrase(shortcut = "omw", expandedText = "On my way can copy this soon!"))
            db.shortcutDao().insert(ShortcutPhrase(shortcut = "ty", expandedText = "Thank you so much!"))
            db.shortcutDao().insert(ShortcutPhrase(shortcut = "colleague", expandedText = "Hi! Just wanted to follow up on our previous conversation regarding the project timeline."))
        }

        // Populate default insights
        val existingInsights = db.insightDao().getAll().firstOrNull()
        if (existingInsights.isNullOrEmpty()) {
            db.insightDao().insert(TypingInsight("words_typed", 1420))
            db.insightDao().insert(TypingInsight("errors_fixed", 82))
            db.insightDao().insert(TypingInsight("tone_rewrites", 41))
            db.insightDao().insert(TypingInsight("smart_replies", 28))
            db.insightDao().insert(TypingInsight("time_saved_mins", 52))
            db.insightDao().insert(TypingInsight("custom_shortcuts_used", 15))
        }

        // Populate default clipboards
        val existingClipboard = db.clipboardDao().getAll().firstOrNull()
        if (existingClipboard.isNullOrEmpty()) {
            db.clipboardDao().insert(ClipboardEntry(text = "Reviewing the contract for the meeting today."))
            db.clipboardDao().insert(ClipboardEntry(text = "https://github.com/keyassist-ai"))
            db.clipboardDao().insert(ClipboardEntry(text = "Hey! Let's schedule the catch up at 4 PM IST."))
        }
    }
}
