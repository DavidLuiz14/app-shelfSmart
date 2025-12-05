package com.example.appshelfsmart.utils

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

object DateUtils {
    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    /**
     * Parse a date string in dd/MM/yyyy format
     */
    fun parseDate(dateString: String): Date? {
        return try {
            dateFormat.parse(dateString)
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Calculate days between a date string and a reference date
     * Returns positive if dateString is in the future, negative if in the past
     */
    fun daysBetween(dateString: String, referenceDate: Date = Date()): Int {
        val date = parseDate(dateString) ?: return 0
        
        val diff = date.time - referenceDate.time
        return TimeUnit.MILLISECONDS.toDays(diff).toInt()
    }

    /**
     * Format days as "hace X días" for past dates
     */
    fun formatDaysAgo(days: Int): String {
        return when {
            days == 0 -> "Hoy"
            days == 1 -> "Hace 1 día"
            days > 1 -> "Hace $days días"
            else -> "En ${-days} días"
        }
    }

    /**
     * Format days until expiration
     * Returns "Caduca en X días" for future, "Caducó hace X días" for past
     */
    fun formatDaysUntil(days: Int): String {
        return when {
            days > 1 -> "Caduca en $days días"
            days == 1 -> "Caduca mañana"
            days == 0 -> "Caduca hoy"
            days == -1 -> "Caducó ayer"
            else -> "Caducó hace ${-days} días"
        }
    }

    /**
     * Check if a date string represents an expired date
     */
    fun isExpired(expirationDate: String): Boolean {
        val date = parseDate(expirationDate) ?: return false
        val today = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.time
        return date.before(today)
    }

    /**
     * Check if a date is expiring soon (within specified days)
     */
    fun isExpiringSoon(expirationDate: String, days: Int = 7): Boolean {
        val date = parseDate(expirationDate) ?: return false
        val today = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        
        val threshold = Calendar.getInstance().apply {
            time = today.time
            add(Calendar.DAY_OF_YEAR, days)
        }
        
        return !date.before(today.time) && !date.after(threshold.time)
    }

    /**
     * Format a timestamp to dd/MM/yyyy
     */
    fun formatDate(timestamp: Long): String {
        return dateFormat.format(Date(timestamp))
    }

    /**
     * Check if a product expires today
     */
    fun isExpiringToday(expirationDate: String): Boolean {
        val date = parseDate(expirationDate) ?: return false
        val today = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        
        val expCal = Calendar.getInstance().apply {
            time = date
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        
        return expCal.timeInMillis == today.timeInMillis
    }

    /**
     * Check if a product expires in 1-3 days
     */
    fun isExpiringIn1to3Days(expirationDate: String): Boolean {
        val days = daysBetween(expirationDate)
        return days in 1..3
    }

    /**
     * Check if a product expires in 4-7 days
     */
    fun isExpiringIn4to7Days(expirationDate: String): Boolean {
        val days = daysBetween(expirationDate)
        return days in 4..7
    }
}
