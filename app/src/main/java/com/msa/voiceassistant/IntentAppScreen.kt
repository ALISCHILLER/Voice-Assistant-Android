package com.msa.voiceassistant

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.tensorflow.lite.Interpreter

@Composable
fun IntentAppScreen(interpreter: Interpreter) {
    var inputText by remember { mutableStateOf("") }
    var predictionText by remember { mutableStateOf("") }

    // نگاشت کلمات به عدد (توکنایزر ساده)
    val tokenizerMap = mapOf(
        "روشن" to 1,
        "کن" to 2,
        "چراغ" to 3,
        "را" to 4,
        "خاموش" to 5,
        "دما" to 6,
        "افزایش" to 7,
        "بده" to 8,
        "کاهش" to 9,
        "تلویزیون" to 10,
        "موزیک" to 11,
        "پخش" to 12,
        "قطع" to 13,
        "کولر" to 14,
        "برو" to 15,
        "به" to 16,
        "اتاق" to 17,
        "نشیمن" to 18,
        "آشپزخانه" to 19,
        "در" to 20,
        "باز" to 21,
        "ببند" to 22,
        "خواب" to 23,
        "چراغ_خواب" to 24
    )

    // لیست برچسب‌های خروجی مدل
    val labels = listOf(
        "light_on",
        "light_off",
        "increase_temperature",
        "decrease_temperature",
        "tv_on",
        "tv_off",
        "play_music",
        "stop_music",
        "ac_on",
        "ac_off",
        "go_to_livingroom",
        "go_to_kitchen",
        "open_door",
        "close_door",
        "bed_light_on",
        "bed_light_off"
    )

    // پیش‌پردازش ورودی: تبدیل متن به آرایه اعداد برای مدل
    fun preprocessInput(text: String): Array<IntArray> {
        val tokens = text.trim().split(" ")
        val maxLen = 10
        val sequence = IntArray(maxLen) { 0 }
        tokens.take(maxLen).forEachIndexed { index, token ->
            sequence[index] = tokenizerMap[token] ?: 0
        }
        return arrayOf(sequence)
    }

    // تابع پیش‌بینی مدل
    fun predict(text: String) {
        if (text.isBlank()) {
            predictionText = "لطفا متنی وارد کنید"
            return
        }

        val input = preprocessInput(text)
        val output = Array(1) { FloatArray(labels.size) }

        // اجرای مدل TFLite
        interpreter.run(input, output)

        // پیدا کردن ایندکس بالاترین احتمال
        val maxIndex = output[0].indices.maxByOrNull { output[0][it] } ?: -1

        predictionText = if (maxIndex >= 0) {
            labels[maxIndex]   // فقط نام فرمان بدون پیشوند
        } else {
            "پیش‌بینی ممکن نیست"
        }
    }

    // UI اصلی
    Column(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth()
    ) {
        Text(text = "تشخیص فرمان با مدل TFLite", style = MaterialTheme.typography.headlineSmall)

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = inputText,
            modifier = Modifier.fillMaxWidth(),
            onValueChange = { inputText = it },
            label = { Text("متن ورودی") }
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { predict(inputText) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("پیش‌بینی کن")
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = predictionText,
            style = MaterialTheme.typography.titleMedium
        )
    }
}
