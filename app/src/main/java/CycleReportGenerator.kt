package com.example.womenhealthtracker

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.widget.Toast
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.*

/**
 * Генератор отчетов о цикле для врача
 */
class CycleReportGenerator(private val context: Context) {
    
    /**
     * Генерирует PDF отчет о цикле
     */
    fun generateCycleReport(): Uri? {
        val userPreferences = UserPreferences(context)
        val userName = userPreferences.getName()
        val cycleLength = userPreferences.getCycleLength()
        val lastPeriodStart = userPreferences.getLastPeriodStart()
        val allDates = userPreferences.getAllDatesWithData()
        
        // Создаем HTML отчет
        val htmlReport = buildString {
            append("<!DOCTYPE html><html><head><meta charset='UTF-8'><title>Отчет о цикле</title>")
            append("<style>")
            append("body { font-family: Arial, sans-serif; padding: 20px; }")
            append("h1 { color: #333; }")
            append("h2 { color: #666; margin-top: 20px; }")
            append("table { width: 100%; border-collapse: collapse; margin-top: 10px; }")
            append("th, td { border: 1px solid #ddd; padding: 8px; text-align: left; }")
            append("th { background-color: #f2f2f2; }")
            append("</style></head><body>")
            
            // Заголовок
            append("<h1>Отчет о менструальном цикле</h1>")
            append("<p><strong>Пациент:</strong> ${if (userName.isNotEmpty()) userName else "Не указано"}</p>")
            append("<p><strong>Дата создания отчета:</strong> ${SimpleDateFormat("d MMMM yyyy, HH:mm", Locale("ru")).format(Date())}</p>")
            
            // Информация о цикле
            append("<h2>Информация о цикле</h2>")
            append("<table>")
            append("<tr><th>Параметр</th><th>Значение</th></tr>")
            append("<tr><td>Длина цикла</td><td>${if (cycleLength > 0) "$cycleLength дней" else "Не указано"}</td></tr>")
            if (lastPeriodStart.isNotEmpty()) {
                val dateFormat = SimpleDateFormat("d MMMM yyyy", Locale("ru"))
                try {
                    val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(lastPeriodStart)
                    if (date != null) {
                        append("<tr><td>Последнее начало цикла</td><td>${dateFormat.format(date)}</td></tr>")
                    }
                } catch (e: Exception) {
                    append("<tr><td>Последнее начало цикла</td><td>$lastPeriodStart</td></tr>")
                }
            }
            append("<tr><td>Всего записей</td><td>${allDates.size}</td></tr>")
            append("</table>")
            
            // Статистика симптомов
            if (allDates.isNotEmpty()) {
                val symptomCounts = mutableMapOf<String, Int>()
                val energyData = mutableListOf<Int>()
                val moodData = mutableListOf<Int>()
                
                allDates.forEach { date ->
                    val dayData = userPreferences.getDayData(date) ?: return@forEach
                    dayData.symptoms.forEach { symptom ->
                        symptomCounts[symptom.name] = (symptomCounts[symptom.name] ?: 0) + 1
                    }
                    dayData.energy?.let { energyData.add(it) }
                    if (dayData.mood >= 0 && dayData.mood <= 4) {
                        moodData.add(dayData.mood)
                    }
                }
                
                if (symptomCounts.isNotEmpty()) {
                    append("<h2>Статистика симптомов</h2>")
                    append("<table>")
                    append("<tr><th>Симптом</th><th>Количество дней</th></tr>")
                    symptomCounts.toList().sortedByDescending { it.second }.forEach { (symptom, count) ->
                        append("<tr><td>$symptom</td><td>$count</td></tr>")
                    }
                    append("</table>")
                }
                
                if (energyData.isNotEmpty()) {
                    val avgEnergy = energyData.average().toInt()
                    append("<h2>Уровень энергии</h2>")
                    append("<p>Средний уровень энергии: $avgEnergy%</p>")
                }
                
                if (moodData.isNotEmpty()) {
                    val avgMood = moodData.average()
                    append("<h2>Настроение</h2>")
                    append("<p>Среднее настроение: ${String.format("%.1f", avgMood)}/4</p>")
                }
            }
            
            // Данные о задержках
            val delayRecords = userPreferences.getDelayRecords()
            if (delayRecords.isNotEmpty()) {
                append("<h2>История задержек</h2>")
                append("<table>")
                append("<tr><th>Дата начала задержки</th><th>Дней задержки</th><th>Основная причина</th></tr>")
                delayRecords.take(5).forEach { record ->
                    val mainReason = record.analyzedReasons.firstOrNull()?.first?.let {
                        when (it) {
                            DelayReason.PREGNANCY -> "Беременность"
                            DelayReason.STRESS -> "Стресс"
                            DelayReason.LIFESTYLE_CHANGE -> "Изменение образа жизни"
                            DelayReason.HORMONAL_FLUCTUATION -> "Гормональные колебания"
                            DelayReason.ILLNESS -> "Заболевание"
                            else -> "Другое"
                        }
                    } ?: "Не указано"
                    append("<tr><td>${record.delayStartDate}</td><td>${record.delayDays}</td><td>$mainReason</td></tr>")
                }
                append("</table>")
            }
            
            append("</body></html>")
        }
        
        // Сохраняем HTML файл
        return try {
            val fileName = "cycle_report_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())}.html"
            val documentsDir = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
            } else {
                File(context.getExternalFilesDir(null), "Documents").apply { mkdirs() }
            }
            
            val file = File(documentsDir, fileName)
            
            FileWriter(file).use { writer ->
                writer.write(htmlReport)
            }
            
            // Возвращаем URI файла через FileProvider для безопасности
            FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
        } catch (e: Exception) {
            android.util.Log.e("CycleReportGenerator", "Ошибка создания отчета: ${e.message}")
            null
        }
    }
    
    /**
     * Показывает диалог с отчетом и опциями поделиться/сохранить
     */
    fun showReportDialog(reportUri: Uri) {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(reportUri, "text/html")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
        }
        
        // Пытаемся открыть отчет
        try {
            context.startActivity(intent)
            Toast.makeText(context, "Отчет создан и открыт", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            // Если не удалось открыть, предлагаем поделиться
            shareReport(reportUri)
        }
    }
    
    /**
     * Позволяет поделиться отчетом
     */
    private fun shareReport(reportUri: Uri) {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/html"
            putExtra(Intent.EXTRA_STREAM, reportUri)
            putExtra(Intent.EXTRA_SUBJECT, "Отчет о менструальном цикле")
            putExtra(Intent.EXTRA_TEXT, "Отчет о моем менструальном цикле")
            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
        }
        
        try {
            context.startActivity(Intent.createChooser(shareIntent, "Поделиться отчетом"))
        } catch (e: Exception) {
            Toast.makeText(context, "Ошибка при попытке поделиться отчетом", Toast.LENGTH_SHORT).show()
        }
    }
}

