package com.msa.voiceassistant

import android.content.Context
import android.util.Log
import org.json.JSONObject

class TokenizerLoader(private val context: Context) {

    private val wordIndex: Map<String, Int>

    init {
        val json = loadJsonFromAsset("tokenizer.json")
        val tokenizer = JSONObject(json)
        val wordIndexStr = tokenizer.optString("word_index", "{}")
        val indexObj = JSONObject(wordIndexStr)
        val map = mutableMapOf<String, Int>()
        indexObj.keys().forEach {
            map[it] = indexObj.getInt(it)
        }
        wordIndex = map
    }

    private fun normalize(text: String): String {
        return text
            .replace("ي", "ی")
            .replace("ك", "ک")
            .replace("\u200c", "") // حذف نیم‌فاصله
            .replace("[^\\p{L}\\p{N}\\s]".toRegex(), "") // حذف علائم
            .trim()
            .lowercase()
    }

    fun tokenize(text: String, maxLen: Int = 10): FloatArray {
        val tokens = normalize(text).split("\\s+".toRegex())
        val result = FloatArray(maxLen) { 0f }
        tokens.take(maxLen).forEachIndexed { index, word ->
            result[index] = (wordIndex[word] ?: 0).toFloat()
        }
        Log.d("TokenizerDebug", "Input: $text → Tokens: $tokens → Encoded: ${result.joinToString()}")
        return result
    }

    fun tokenizeInt(text: String, maxLen: Int = 10): IntArray {
        val tokens = normalize(text).split("\\s+".toRegex())
        return IntArray(maxLen) { i -> wordIndex[tokens.getOrNull(i)] ?: 0 }
    }

    private fun loadJsonFromAsset(filename: String): String {
        return context.assets.open(filename).bufferedReader().use { it.readText() }
    }
}
