package com.malopieds.innertune

// 1. Primero, añade estos permisos en AndroidManifest.xml
// <uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
// <uses-permission android:name="android.permission.INTERNET" />

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.*
import androidx.work.WorkerParameters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.URL
import java.util.concurrent.TimeUnit

class UpdateCheckerWorker(
    private val context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    companion object {
        private const val CHANNEL_ID = "app_updates"
        private const val NOTIFICATION_ID = 1
    }

    override suspend fun doWork(): Result {
        try {
            val currentVersion = context.packageManager
                .getPackageInfo(context.packageName, 0).versionName

            val latestVersion = checkForUpdates()

            if (latestVersion != null && isNewerVersion(latestVersion, currentVersion)) {
                createNotificationChannel()
                showUpdateNotification(latestVersion)
            }
            return Result.success()
        } catch (e: Exception) {
            return Result.failure()
        }
    }

    private suspend fun checkForUpdates(): String? = withContext(Dispatchers.IO) {
        try {
            val url = URL("https://api.github.com/repos/Arturo254/InnerTune/releases/latest")
            val connection = url.openConnection()
            connection.connect()
            val json = connection.getInputStream().bufferedReader().use { it.readText() }
            val jsonObject = JSONObject(json)
            return@withContext jsonObject.getString("tag_name")
        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext null
        }
    }

    private fun isNewerVersion(remoteVersion: String, currentVersion: String): Boolean {
        val remote = remoteVersion.removePrefix("v").split(".").map { it.toIntOrNull() ?: 0 }
        val current = currentVersion.removePrefix("v").split(".").map { it.toIntOrNull() ?: 0 }

        for (i in 0 until maxOf(remote.size, current.size)) {
            val r = remote.getOrNull(i) ?: 0
            val c = current.getOrNull(i) ?: 0
            if (r > c) return true
            if (r < c) return false
        }
        return false
    }

    private fun createNotificationChannel() {
        val name = "Actualizaciones de la aplicación"
        val descriptionText = "Notificaciones sobre nuevas versiones disponibles"
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
            description = descriptionText
        }

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    private fun showUpdateNotification(newVersion: String) {
        // Intent para abrir el repositorio de GitHub
        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse("https://github.com/Arturo254/InnerTune/releases/latest")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.opentune_monochrome)
            .setContentTitle(context.getString(R.string.NewVersion))
            .setContentText(" $newVersion")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, builder.build())
    }
}

// Clase para manejar la programación de las verificaciones
class UpdateManager(private val context: Context) {

    fun scheduleUpdateChecks() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val updateCheckRequest = PeriodicWorkRequestBuilder<UpdateCheckerWorker>(
            6, TimeUnit.HOURS, // Verifica cada 6 horas
            30, TimeUnit.MINUTES // Flexibilidad de 30 minutos
        )
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(context)
            .enqueueUniquePeriodicWork(
                "version_check",
                ExistingPeriodicWorkPolicy.UPDATE,
                updateCheckRequest
            )
    }
}