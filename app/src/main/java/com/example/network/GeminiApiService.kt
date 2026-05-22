package com.example.network

import com.example.BuildConfig
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

interface GeminiApiService {
    // We use gemini-3.5-flash as the default for basic text tasks
    @POST("v1beta/models/gemini-3.5-flash:generateContent")
    suspend fun generateContent(
        @Query("key") apiKey: String,
        @Body request: GeminiRequest
    ): GeminiResponse
}

object RetrofitClient {
    private const val BASE_URL = "https://generativelanguage.googleapis.com/"

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    val apiService: GeminiApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(GeminiApiService::class.java)
    }
}

class GeminiRepository {
    private val api = RetrofitClient.apiService

    suspend fun processText(
        systemPrompt: String,
        userPrompt: String,
        temperature: Float = 0.5f
    ): Result<String> {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            return Result.failure(Exception("Gemini API key is not set. Please use the Secrets panel in AI Studio to add GEMINI_API_KEY."))
        }

        val request = GeminiRequest(
            contents = listOf(
                GeminiContent(parts = listOf(GeminiPart(text = userPrompt)))
            ),
            systemInstruction = GeminiContent(
                parts = listOf(GeminiPart(text = systemPrompt))
            ),
            generationConfig = GeminiGenerationConfig(
                temperature = temperature,
                maxOutputTokens = 1000
            )
        )

        return try {
            val response = api.generateContent(apiKey, request)
            val text = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
            if (text != null) {
                Result.success(text.trim())
            } else {
                Result.failure(Exception("Empty API response."))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Specific AI Utilities
    suspend fun correctGrammar(text: String): Result<String> {
        val sys = "You are a professional writing assistant. Identify and correct any grammatical errors, spelling list mistakes, and punctuation slipups in the input text. Maintain the original message structure. ONLY respond with the corrected text itself. DO NOT add any explanations, introductory notes, or surrounding quotes."
        return processText(sys, text, 0.2f)
    }

    suspend fun getSmartReplies(context: String): Result<List<String>> {
        val sys = "You are a smart chat assistant. Analyze the incoming chat message or conversation context and generate 3 short, contextual, natural-sounding smart replies. The replies should range in emotional style (one enthusiastic/yes, one professional/neutral, one quick check-in). Output ONLY the replies as a raw bulleted list starting with '- ' with nothing else."
        val res = processText(sys, context, 0.7f)
        return res.map { text ->
            text.lines()
                .filter { it.startsWith("-") || it.startsWith("*") }
                .map { it.replaceFirst(Regex("^[-*]\\s*"), "").trim() }
                .filter { it.isNotEmpty() }
                .take(3)
                .ifEmpty { listOf("Got it!", "Thanks!", "I'll follow up soon.") }
        }
    }

    suspend fun changeTone(text: String, tone: String): Result<String> {
        val toneInstruction = when (tone.lowercase()) {
            "professional" -> "Write this in a clear, polished, professional, and business-ready tone suitable for corporate communication. Do not be overly verbose but be polite."
            "friendly" -> "Write this in a warm, friendly, casual, and highly approachable tone suitable for close coworkers or friends. Add 1-2 appropriate emojis."
            "formal" -> "Write this in a very respectful, formal, elegant, and diplomatic tone. Use proper salutations if needed and maintain high etiquette."
            "funny" -> "Write this in a hilarious, witty, lighthearted, and slightly sarcastic or playful tone. Keep it highly engaging and add some humor/emoji."
            else -> "Rewrite the text nicely."
        }
        val sys = "You are a master tone editor. $toneInstruction Do NOT include any intro/outro like 'Here is the rewritten text:'. ONLY yield the rewritten text itself. If it is already fine, return a nicely updated version of it. Do not wrap the output in quotes."
        return processText(sys, text, 0.6f)
    }

    suspend fun hingeToEng(text: String): Result<String> {
        val sys = "You are an expert bilingual Hinglish to English translation assistant. Translate Hinglish input text (Hindi written in Roman/English characters, combined with casual English slang) into grammatically perfect, natural, and standard English. Output ONLY the English translation. No explanations, no notes."
        return processText(sys, text, 0.3f)
    }

    suspend fun autoCompleteText(text: String): Result<String> {
        val sys = "You are an autocomplete engine above a mobile keyboard. Complete the sentence or phrase provided by the user. Give ONLY the remaining completed part of the sentence (e.g. if the user says 'Hey, just following', you can return ' up on our project proposal. Let me know when you are free.'). Give 1 strong natural sentence completion. Keep it short (under 12 words) and highly probable. Only return the completion portion."
        return processText(sys, text, 0.5f)
    }

    suspend fun paraphrase(text: String): Result<String> {
        val sys = "You are a paraphrasing tool. Rewrite the input text in a fresh, engaging way while keeping the exact original intent and details. Provide ONLY the paraphrased sentence. Do not add intro text, quotes, or explanations."
        return processText(sys, text, 0.7f)
    }
}
