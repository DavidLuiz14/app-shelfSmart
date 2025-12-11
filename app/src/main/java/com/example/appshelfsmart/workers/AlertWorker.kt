package com.example.appshelfsmart.workers

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.appshelfsmart.MainActivity
import com.example.appshelfsmart.R // Assuming R is available, otherwise will rely on android.R
import com.example.appshelfsmart.data.database.AppDatabase
import com.example.appshelfsmart.utils.DateUtils
import kotlinx.coroutines.flow.first

class AlertWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val database = AppDatabase.getDatabase(applicationContext)
        val productDao = database.productDao()
        val inventoryItems = productDao.getAllProducts().first()

        val expiringSoon = inventoryItems.filter { 
            DateUtils.isExpiringSoon(it.expirationDate, 3) 
        }

        // Logic matched from ProductViewModel
        val lowStockGroups = inventoryItems
            .groupBy { it.barcode.ifBlank { "${it.name}|${it.brand}" } }
            .mapNotNull { (_, group) ->
                val totalUnits = group.sumOf { it.units }
                if (totalUnits <= 2) {
                     // Return a representative with updated units
                     val representative = group.maxByOrNull { it.purchaseDate }
                     representative?.copy(units = totalUnits)
                } else {
                    null
                }
            }

        if (expiringSoon.isNotEmpty() || lowStockGroups.isNotEmpty()) {
            val expiringNames = expiringSoon.map { it.name }
            sendNotification(expiringNames, lowStockGroups.size)
        }

        return Result.success()
    }

    private fun sendNotification(expiringNames: List<String>, lowStockCount: Int) {
        val channelId = "shelf_smart_alerts"
        val context = applicationContext

        // Create Channel (Android 8.0+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "ShelfSmart Alerts"
            val descriptionText = "Notifications for expiring items and low stock"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(channelId, name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }

        // Create Intent to open MainActivity
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("navigate_to", "alerts")
        }
        val pendingIntent: PendingIntent = PendingIntent.getActivity(
            context, 0, intent, PendingIntent.FLAG_IMMUTABLE
        )

        // Build Notification Content
        val title = "ShelfSmart Alert"
        val contentText = StringBuilder()
        
        if (expiringNames.isNotEmpty()) {
            val namesString = expiringNames.joinToString(", ")
            contentText.append("Expiring: $namesString. ")
        }
        
        if (lowStockCount > 0) {
           contentText.append("$lowStockCount product(s) low on stock.")
        }

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(contentText.toString())
            .setStyle(NotificationCompat.BigTextStyle().bigText(contentText.toString()))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        // Send
        try {
            // Check permission for Android 13+ is handled in Activity, 
            // but for Worker we just try. If permission missing, it won't show.
            with(NotificationManagerCompat.from(context)) {
                 notify(1001, builder.build())
            }
        } catch (e: SecurityException) {
            // Permission not granted
        }
    }
}
