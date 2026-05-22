package com.example.ui

import android.app.Application
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import com.example.network.GeminiRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Locale

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val repository = AppRepository(db)
    private val geminiRepository = GeminiRepository()

    // --- State Definitions ---
    
    // Core Navigation: Screen determines active flow
    enum class Screen {
        Onboarding,
        Dashboard,
        KeyboardSimulator,
        ClipboardHistory,
        ShortcutsManager,
        TypingInsights,
        LanguageConfig,
        VoiceHelper,
        PremiumCenter,
        Settings
    }

    private val _currentScreen = MutableStateFlow(Screen.Onboarding)
    val currentScreen: StateFlow<Screen> = _currentScreen.asStateFlow()

    // Onboarding preferences
    val userName = MutableStateFlow("Explorer")
    val userStylePreference = MutableStateFlow("Futuristic Neon") // "Classic Gboard", "Glassmorphism Dark"
    val preferredLanguage = MutableStateFlow("English")

    // Database Flows (Observable states)
    val clipboardHistory: StateFlow<List<ClipboardEntry>> = repository.clipboardEntries
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val shortcutPhrases: StateFlow<List<ShortcutPhrase>> = repository.shortcutPhrases
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _rawInsights = repository.typingInsights
    val typingInsights: StateFlow<Map<String, Int>> = _rawInsights
        .map { list -> list.associate { it.key to it.count } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    // --- Simulated Chat Interaction & Keyboard Input State ---
    
    data class ChatMessage(
        val sender: String,
        val text: String,
        val timestamp: Long = System.currentTimeMillis(),
        val isMe: Boolean
    )

    private val _simulatedChatHistory = MutableStateFlow<List<ChatMessage>>(emptyList())
    val simulatedChatHistory: StateFlow<List<ChatMessage>> = _simulatedChatHistory.asStateFlow()

    val keyboardInputBuffer = MutableStateFlow("") // Active typing buffer of keyboard
    val activeChatPartnerName = MutableStateFlow("Sarah (Boss)") // Sarah (Boss), Rohan (Friend), Alex (Client)
    val activeChatPartnerAvatarColor = MutableStateFlow(0xFF00E5FF)

    // Keyboard configuration overlays
    val isTonePopupVisible = MutableStateFlow(false)
    val isClipboardPanelVisible = MutableStateFlow(false)
    val isShortcutPanelVisible = MutableStateFlow(false)
    val isLanguageSelectionVisible = MutableStateFlow(false)
    val isVoiceOverlayVisible = MutableStateFlow(false)

    // Active operational metrics
    val aiLoading = MutableStateFlow(false)
    val apiErrorMessage = MutableStateFlow("")
    val isPremiumUser = MutableStateFlow(false)

    // Auto-completions & AI responses floating list
    private val _aiSuggestions = MutableStateFlow<List<String>>(listOf("Let's do it!", "I'll review and reply soon.", "Sounds awesome."))
    val aiSuggestions: StateFlow<List<String>> = _aiSuggestions.asStateFlow()

    // Voice to text engine helper
    private var speechRecognizer: SpeechRecognizer? = null
    val voiceListeningState = MutableStateFlow(false) // Whether listening or not
    val voiceVolumeLevel = MutableStateFlow(0f) // Simulated volume (0f to 10f) for pulsing bar styling

    // Settings Flags
    val hapticFeedbackEnabled = MutableStateFlow(true)
    val autoCapitalizationEnabled = MutableStateFlow(true)
    val doubleSpacePeriodEnabled = MutableStateFlow(true)
    val blockOffensiveWords = MutableStateFlow(true)

    init {
        viewModelScope.launch {
            repository.populateDefaultsIfNeeded()
            resetChatSimulator("Sarah (Boss)")
        }
    }

    fun navigateTo(screen: Screen) {
        _currentScreen.value = screen
    }

    // --- Onboarding Completion ---
    fun finishOnboarding(name: String, style: String, lang: String) {
        userName.value = name.ifBlank { "Explorer" }
        userStylePreference.value = style
        preferredLanguage.value = lang
        _currentScreen.value = Screen.Dashboard
    }

    // --- Chat Simulator Reset/Toggle ---
    fun resetChatSimulator(partner: String) {
        activeChatPartnerName.value = partner
        val msgs = when (partner) {
            "Sarah (Boss)" -> {
                activeChatPartnerAvatarColor.value = 0xFF2979FF
                listOf(
                    ChatMessage("Sarah (Boss)", "Hi, did you review the project proposals I sent yesterday? We need comments by EOD.", System.currentTimeMillis() - 7200000, false),
                    ChatMessage("Sarah (Boss)", "Also, make sure the budget estimation matches our current target layout. Let me know details.", System.currentTimeMillis() - 3600000, false)
                )
            }
            "Rohan (Friend)" -> {
                activeChatPartnerAvatarColor.value = 0xFFE040FB
                listOf(
                    ChatMessage("Rohan (Friend)", "Aaja bhai! Aaj raat dinner ka kya plan hai? Kya khana khayenge?", System.currentTimeMillis() - 5000000, false),
                    ChatMessage("Rohan (Friend)", "Koyla restaurant chalte hain na? Wahin par discussion karenge.", System.currentTimeMillis() - 1000000, false)
                )
            }
            else -> { // Alex (Client)
                activeChatPartnerAvatarColor.value = 0xFF00E5FF
                listOf(
                    ChatMessage("Alex (Client)", "Thanks for sending over the prototype. Some features look promising, but is it secure?", System.currentTimeMillis() - 4000000, false)
                )
            }
        }
        _simulatedChatHistory.value = msgs
        keyboardInputBuffer.value = ""
        triggerSmartAIReplies(msgs.lastOrNull()?.text ?: "")
    }

    // --- Core AI Keyboard Features ---

    // 1. Grammer Correction
    fun triggerGrammarCorrection() {
        val currentText = keyboardInputBuffer.value
        if (currentText.isBlank()) return
        viewModelScope.launch {
            aiLoading.value = true
            apiErrorMessage.value = ""
            val result = geminiRepository.correctGrammar(currentText)
            if (result.isSuccess) {
                val corrected = result.getOrNull() ?: currentText
                keyboardInputBuffer.value = corrected
                repository.incrementInsight("errors_fixed")
                if (currentText != corrected) {
                    repository.incrementInsight("time_saved_mins", 1)
                }
            } else {
                val error = result.exceptionOrNull()
                apiErrorMessage.value = error?.message ?: "Failed standard grammar correction"
            }
            aiLoading.value = false
        }
    }

    // 2. Hinglish to English translation
    fun triggerHinglishToEnglish() {
        val currentText = keyboardInputBuffer.value
        if (currentText.isBlank()) return
        viewModelScope.launch {
            aiLoading.value = true
            apiErrorMessage.value = ""
            val result = geminiRepository.hingeToEng(currentText)
            if (result.isSuccess) {
                val englishText = result.getOrNull() ?: currentText
                keyboardInputBuffer.value = englishText
                repository.incrementInsight("tone_rewrites")
                repository.incrementInsight("time_saved_mins", 2)
            } else {
                val error = result.exceptionOrNull()
                apiErrorMessage.value = error?.message ?: "Failed Hinglish conversion"
            }
            aiLoading.value = false
        }
    }

    // 3. Tone rewrite changer
    fun triggerToneRewrite(tone: String) {
        val currentText = keyboardInputBuffer.value
        if (currentText.isBlank()) return
        viewModelScope.launch {
            aiLoading.value = true
            apiErrorMessage.value = ""
            isTonePopupVisible.value = false
            val result = geminiRepository.changeTone(currentText, tone)
            if (result.isSuccess) {
                val rewritten = result.getOrNull() ?: currentText
                keyboardInputBuffer.value = rewritten
                repository.incrementInsight("tone_rewrites")
                repository.incrementInsight("time_saved_mins", 3)
            } else {
                val error = result.exceptionOrNull()
                apiErrorMessage.value = error?.message ?: "Failed tone rewrite"
            }
            aiLoading.value = false
        }
    }

    // 4. Paraphrase trigger
    fun triggerParaphrase() {
        val currentText = keyboardInputBuffer.value
        if (currentText.isBlank()) return
        viewModelScope.launch {
            aiLoading.value = true
            apiErrorMessage.value = ""
            val result = geminiRepository.paraphrase(currentText)
            if (result.isSuccess) {
                val paraphrased = result.getOrNull() ?: currentText
                keyboardInputBuffer.value = paraphrased
                repository.incrementInsight("tone_rewrites")
            } else {
                val error = result.exceptionOrNull()
                apiErrorMessage.value = error?.message ?: "Failed paraphrase"
            }
            aiLoading.value = false
        }
    }

    // 5. Autocomplete suggestions based on buffer of text
    fun triggerAutoComplete() {
        val currentText = keyboardInputBuffer.value
        if (currentText.isBlank()) return
        viewModelScope.launch {
            val result = geminiRepository.autoCompleteText(currentText)
            if (result.isSuccess) {
                val completion = result.getOrNull() ?: ""
                if (completion.isNotBlank() && completion.length < 50) {
                    _aiSuggestions.value = listOf(completion) + _aiSuggestions.value.take(2)
                }
            }
        }
    }

    // 6. Context-based Suggest Smart Replies
    fun triggerSmartAIReplies(incomingContext: String) {
        if (incomingContext.isBlank()) return
        viewModelScope.launch {
            val result = geminiRepository.getSmartReplies(incomingContext)
            if (result.isSuccess) {
                val replies = result.getOrNull() ?: emptyList()
                _aiSuggestions.value = replies
                repository.incrementInsight("smart_replies")
            }
        }
    }

    // 7. Template insertion
    fun insertTemplate(templateText: String) {
        keyboardInputBuffer.value = templateText
    }

    // --- Clipboard management features ---
    fun addClipboardText(text: String) {
        viewModelScope.launch {
            repository.addClipboardEntry(text)
        }
    }

    fun deleteClipboardItem(id: Int) {
        viewModelScope.launch {
            repository.deleteClipboardEntry(id)
        }
    }

    fun clearClipboard() {
        viewModelScope.launch {
            repository.clearClipboard()
        }
    }

    // --- Shortcuts Management features ---
    fun addShortcutPhrase(shortcut: String, expanded: String) {
        viewModelScope.launch {
            repository.addShortcut(shortcut, expanded)
        }
    }

    fun deleteShortcutPhrase(id: Int) {
        viewModelScope.launch {
            repository.deleteShortcut(id)
        }
    }

    // --- Main Messaging Simulated Sending ---
    fun sendSimulatedMessage() {
        val currentInput = keyboardInputBuffer.value
        if (currentInput.isBlank()) return

        // Save entry into chat
        val textToSend = checkAndExpandShortcuts(currentInput)
        val wordCount = textToSend.split("\\s+".toRegex()).size

        val myMsg = ChatMessage("Me", textToSend, System.currentTimeMillis(), true)
        _simulatedChatHistory.value = _simulatedChatHistory.value + myMsg
        keyboardInputBuffer.value = ""

        // Update statistics
        viewModelScope.launch {
            repository.incrementInsight("words_typed", wordCount)
            
            // Auto add to clipboard occasionally to demonstrate Gboard clipboard helper
            if (myMsg.text.length > 5) {
                repository.addClipboardEntry(myMsg.text)
            }

            // Simulate automatic response from conversational partner
            simulatePartnerTypingAndReply()
        }
    }

    private fun checkAndExpandShortcuts(input: String): String {
        val words = input.split(" ")
        val expandedWords = words.map { word ->
            val cleanWord = word.trim().lowercase().replace(Regex("[^a-z0-9]"), "")
            val shortcutMatch = shortcutPhrases.value.find { it.shortcut == cleanWord }
            if (shortcutMatch != null) {
                viewModelScope.launch {
                    repository.incrementInsight("custom_shortcuts_used")
                    repository.incrementInsight("time_saved_mins", 1)
                }
                shortcutMatch.expandedText
            } else {
                word
            }
        }
        return expandedWords.joinToString(" ")
    }

    private fun simulatePartnerTypingAndReply() {
        val partner = activeChatPartnerName.value
        viewModelScope.launch {
            kotlinx.coroutines.delay(2000) // Delay to simulate writing
            val replyText = when (partner) {
                "Sarah (Boss)" -> "Sounds good. Please upload the finalized details onto our dashboard, and let me know."
                "Rohan (Friend)" -> "Wah perfect! 9 baje milte hain fir wahan. Bhai please late mat hona."
                else -> "Great points. Security is indeed our top priority here. Let's touch base tomorrow."
            }
            val partnerMsg = ChatMessage(partner, replyText, System.currentTimeMillis(), false)
            _simulatedChatHistory.value = _simulatedChatHistory.value + partnerMsg

            // Fetch new smart options based on partner's last text
            triggerSmartAIReplies(replyText)
        }
    }

    // --- Speech Recognition Logic ---
    fun startVoiceTyping() {
        val context = getApplication<Application>()
        if (SpeechRecognizer.isRecognitionAvailable(context)) {
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
                putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
            }
            try {
                speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                    setRecognitionListener(object : RecognitionListener {
                        override fun onReadyForSpeech(params: Bundle?) {
                            voiceListeningState.value = true
                            voiceVolumeLevel.value = 2f
                        }
                        override fun onBeginningOfSpeech() {
                            voiceVolumeLevel.value = 4f
                        }
                        override fun onRmsChanged(rmsdB: Float) {
                            // Normalize RMS to 0f-10f scale
                            val level = (rmsdB + 2f).coerceIn(0f, 10f)
                            voiceVolumeLevel.value = level
                        }
                        override fun onBufferReceived(buffer: ByteArray?) {}
                        override fun onEndOfSpeech() {
                            voiceListeningState.value = false
                            voiceVolumeLevel.value = 0f
                        }
                        override fun onError(error: Int) {
                            voiceListeningState.value = false
                            voiceVolumeLevel.value = 0f
                        }
                        override fun onResults(results: Bundle?) {
                            val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                            val recognizedText = matches?.firstOrNull()
                            if (!recognizedText.isNullOrBlank()) {
                                keyboardInputBuffer.value = keyboardInputBuffer.value + " " + recognizedText
                                viewModelScope.launch {
                                    repository.incrementInsight("words_typed", recognizedText.split(" ").size)
                                }
                            }
                        }
                        override fun onPartialResults(partialResults: Bundle?) {}
                        override fun onEvent(eventType: Int, params: Bundle?) {}
                    })
                    startListening(intent)
                }
            } catch (e: Exception) {
                simulateVoiceTyping()
            }
        } else {
            simulateVoiceTyping()
        }
    }

    fun stopVoiceTyping() {
        speechRecognizer?.stopListening()
        speechRecognizer?.destroy()
        speechRecognizer = null
        voiceListeningState.value = false
        voiceVolumeLevel.value = 0f
    }

    private fun simulateVoiceTyping() {
        // Fallback for situations where permission is not ready or emulator environment lacks it
        voiceListeningState.value = true
        viewModelScope.launch {
            // Pulse simulated values
            var tick = 0
            while (voiceListeningState.value && tick < 10) {
                voiceVolumeLevel.value = (3..9).random().toFloat()
                kotlinx.coroutines.delay(400)
                tick++
            }
            if (voiceListeningState.value) {
                val phrase = listOf(
                    "Hey there keyassist is awesome",
                    "Please rewrite my last message professionally",
                    "Let's catch up later tonight",
                    "Haan mai thoda late ho jaunga"
                ).random()
                keyboardInputBuffer.value = keyboardInputBuffer.value + " " + phrase
                repository.incrementInsight("words_typed", phrase.split(" ").size)
                stopVoiceTyping()
            }
        }
    }

    // --- Subscription Purchase Trigger ---
    fun purchasePremium() {
        viewModelScope.launch {
            isPremiumUser.value = true
            // Boost insights to feel premium
            repository.incrementInsight("time_saved_mins", 100)
        }
    }

    fun cancelPremium() {
        isPremiumUser.value = false
    }

    override fun onCleared() {
        super.onCleared()
        speechRecognizer?.destroy()
    }
}
