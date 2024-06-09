package com.example.foregroundservice


import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.*
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.SocketException
import java.net.URL


class MyDownloadService : Service() {
    private lateinit var overlayView: OverlayView
    private val STORAGE_DIRECTORY = "/Download/TestFolder"
    companion object {
        const val CHANNEL_ID = "download_channel_id"
        const val NOTIFICATION_ID = 1
    }
    @SuppressLint("ForegroundServiceType")
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        val notification = startForegroundService()
        startForeground(NOTIFICATION_ID,notification)
        overlayView = OverlayView(this)
//        overlayView.showOverlay()
        val fileName = System.currentTimeMillis().toString().replace(":", ".") + ".mp3"
        downloadFile("https://pagalfree.com/musics/128-Rabba%20Mereya%20-%20B%20Praak%20128%20Kbps.mp3", fileName)
        return START_STICKY
    }

    @SuppressLint("SuspiciousIndentation")
    private fun broadcastProgress(progress: String) {
        Log.d("MyDownloadService", "Broadcasting progress update: $progress")
        val intent = Intent("com.example.ACTION_DOWNLOAD_PROGRESS")
        intent.putExtra("progress", progress)
        sendBroadcast(intent)
        Log.d("send", "My send data ${sendBroadcast(intent)}")
//        overlayView.updateProgress(progress.toInt())
    }

    private fun downloadFile(mUrl: String, fileName: String) {
        val storageDirectory = Environment.getExternalStorageDirectory().toString() + STORAGE_DIRECTORY + "/${fileName}"
        val file = File(Environment.getExternalStorageDirectory().toString() + STORAGE_DIRECTORY)
        if (!file.exists()) {
            file.mkdirs()
        }
        GlobalScope.launch(Dispatchers.IO) {
            val url = URL(mUrl)
            var connection: HttpURLConnection? = null
            try {
                connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "GET"
                connection.setRequestProperty("Accept-Encoding", "identity")
                connection.connect()
                if (connection.responseCode in 200..299) {
//                    withContext(Dispatchers.Main) {
//                        binding.progressBar.progress = 0
//                    }
//                    handler.post {
//                        Toast.makeText(applicationContext, "This is my message",Toast.LENGTH_LONG).show()
//                    }
                    val fileSize = connection.contentLength
                    val inputStream = connection.inputStream
                    val outputStream = FileOutputStream(storageDirectory)

                    var bytesCopied: Long = 0
                    var buffer = ByteArray(1024)
                    var bytes = inputStream.read(buffer)
                    while (bytes >= 0) {
                        bytesCopied += bytes
                        val downloadProgress = (bytesCopied.toFloat() / fileSize.toFloat() * 100).toInt()
//                        withContext(Dispatchers.Main) {
//                            binding.progressBar.progress = downloadProgress
//                            binding.textProgress.text = "$downloadProgress"
                        broadcastProgress(downloadProgress.toString())
//                        }
                        outputStream.write(buffer, 0, bytes)
                        bytes = inputStream.read(buffer)
                    }
                    outputStream.close()
                    inputStream.close()
                } else {
                    Log.d("TAG","Connection Error: ${connection.responseCode}")

                }
            } catch (e: SocketException) {
                Log.d("TAG","Connection Reset. Retrying...")
                // Handle socket exception (connection reset) by retrying the download
                downloadFile(mUrl, fileName)
            } catch (e: Exception) {
                Log.d("TAG","Error: ${e.message}")
                // Handle other exceptions
            } finally {
                connection?.disconnect() // Disconnect the connection
            }
        }
    }


    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    @SuppressLint("ForegroundServiceType")
    private fun startForegroundService() : Notification {
        createNotificationChannel()

        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            notificationIntent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Foreground Service")
            .setContentText("Downloading...")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(pendingIntent)
            .build()

        return notification
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelName = "Download Channel"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, channelName, importance)
            val notificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
    override fun stopService(name: Intent?): Boolean {
        return super.stopService(name)
        Log.d("Stopping","Stopping Service")
    }

    override fun onDestroy() {
        Toast.makeText(
            applicationContext, "Service execution completed",
            Toast.LENGTH_SHORT
        ).show()
        if (overlayView!= null) {
            overlayView.hideOverlay()
            Log.d("overlay","overlay is hide")
        }
        Log.d("Stopped","Service Stopped")
        super.onDestroy()
    }
}