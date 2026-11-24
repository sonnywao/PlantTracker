package com.example.planttracker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.Worker
import androidx.work.WorkerParameters

class WaterReminderWorker(
    context: Context,
    params: WorkerParameters
) : Worker(context, params) {

    override fun doWork(): Result {
        val repo = PlantRepository(applicationContext)
        val plants = repo.load()
        val now = System.currentTimeMillis()
        val day = 24L * 60 * 60 * 1000

        val needWater = plants.filter { (now - it.lastWatered) / day >= 7 }

        if (needWater.isNotEmpty()) {
            showNotification(needWater.map { it.name })
        }
        return Result.success()
    }

    private fun showNotification(names: List<String>) {
        val channelId = "water_channel"
        val manager = applicationContext
            .getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Напоминания о поливе",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            manager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(applicationContext, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Пора полить растения")
            .setContentText(names.joinToString(", "))
            .setAutoCancel(true)
            .build()

        manager.notify(1, notification)
    }
}