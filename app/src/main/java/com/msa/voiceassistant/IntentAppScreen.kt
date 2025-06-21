package com.msa.voiceassistant

import android.content.Context
import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.tensorflow.lite.Interpreter



@Composable
fun IntentAppScreen(interpreter: Interpreter, context: Context) {
    var inputText by remember { mutableStateOf("") }
    var predictionText by remember { mutableStateOf("") }

    // نمونه‌سازی توکنایزر فقط یکبار با remember
    val tokenizerLoader = remember { TokenizerLoader(context) }

    // لیبل‌های خروجی مدل
    val labels = listOf(
        "light_on", "light_off", "increase_temperature", "decrease_temperature",
        "tv_on", "tv_off", "play_music", "stop_music",
        "ac_on", "ac_off", "go_to_livingroom", "go_to_kitchen",
        "open_door", "close_door", "bed_light_on", "bed_light_off"
    )

    fun predict(text: String) {
        Log.d("IntentApp", "Input text: $text")

        if (text.isBlank()) {
            predictionText = "لطفاً متنی وارد کنید"
            return
        }

        // تبدیل متن ورودی به آرایه توکن‌ها
        val input = arrayOf(tokenizerLoader.tokenize(text))

        // آرایه خروجی مدل (یک نمونه با طول برابر تعداد لیبل‌ها)
        val output = Array(1) { FloatArray(labels.size) }

        try {
            interpreter.run(input, output)
            val maxIndex = output[0].indices.maxByOrNull { output[0][it] } ?: -1
            predictionText = if (maxIndex >= 0) labels[maxIndex] else "نتیجه‌ای یافت نشد"
            Log.d("IntentApp", "Model output: ${output[0].joinToString()}")
            Log.d("IntentApp", "Predicted label: $predictionText")
        } catch (e: Exception) {
            predictionText = "❌ خطا در اجرا: ${e.message}"
            Log.e("IntentApp", "Error running model", e)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(text = "تشخیص فرمان صوتی", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = inputText,
            onValueChange = { inputText = it },
            label = { Text("دستور را وارد کنید") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = { predict(inputText) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("تشخیص بده")
        }
        Spacer(modifier = Modifier.height(24.dp))
        Text(text = predictionText, style = MaterialTheme.typography.titleMedium)
    }
}
