package com.example.womenhealthtracker

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

class NotificationReceiver : BroadcastReceiver() {
    
    override fun onReceive(context: Context, intent: Intent) {
        val notificationId = intent.getStringExtra("notification_id") ?: return
        val title = intent.getStringExtra("notification_title") ?: "Напоминание"
        val body = intent.getStringExtra("notification_body") ?: ""
        val type = intent.getStringExtra("notification_type") ?: ""
        
        // Проверяем разрешение на уведомления перед показом
        val notificationManager = NotificationManagerCompat.from(context)
        
        // Проверяем, есть ли разрешение на уведомления
        if (!notificationManager.areNotificationsEnabled()) {
            // Если разрешения нет, не показываем уведомление
            return
        }
        
        val notification = NotificationCompat.Builder(context, NotificationHelper.CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()
        
        try {
            notificationManager.notify(notificationId.hashCode(), notification)
        } catch (e: SecurityException) {
            // Если нет разрешения, игнорируем ошибку
            android.util.Log.e("NotificationReceiver", "Нет разрешения на показ уведомлений", e)
        }
    }
}



