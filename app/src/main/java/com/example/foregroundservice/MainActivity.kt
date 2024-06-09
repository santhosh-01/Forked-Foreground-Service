package com.example.foregroundservice

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.foregroundservice.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private val receiver = MyReceiver()
    private lateinit var overlayView: OverlayView
    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        overlayView = OverlayView(this)

        binding.download.setOnClickListener {
//            Toast.makeText(this, "start download", Toast.LENGTH_SHORT).show()
//            val serviceIntent = Intent(this, MyDownloadService::class.java)
//            ContextCompat.startForegroundService(this, serviceIntent)
            startService()

        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onStart() {
        super.onStart()
        val intentFilter = IntentFilter("com.example.ACTION_DOWNLOAD_PROGRESS")
        val a =   registerReceiver(receiver, intentFilter, RECEIVER_EXPORTED)
        Log.d("receiver", "Received $a")
//        overlayView = OverlayView(this)
//        overlayView.hideOverlay()
        checkOverlayPermission()
    }

    inner class MyReceiver : BroadcastReceiver(){
        override fun onReceive(context: Context?, intent: Intent?) {
            Log.d("MyReceiver", "Received progress update broadcast")
            val progress = intent?.getStringExtra("progress")
            // Update progress bar in your activity
            overlayView.updateProgress(progress!!.toInt())
            Log.d("overlay send", "My send data ${overlayView.updateProgress(progress.toInt())}")
//            Log.d("overlay send", "My send data ${overlayView.showOverlay()}")
            progress?.let {
                Log.d("MyReceiver", "Received progress value: $it")
                binding.progressBar.progress = it.toInt()
                Log.d("TAG3", "Progress Update ${ binding.progressBar.progress}")
                binding.textProgress.text = it.toString()
                Log.d("TAG4", "Progress Update ${ binding.progressBar.progress}")
            }
        }
    }


    //bs ye subha handle karny h is ko ek btn mein lgao or dosri ko onpause mein laga kar check karo
    // method for starting the service
    fun startService() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // check if the user has already granted
            // the Draw over other apps permission
            if (Settings.canDrawOverlays(this)) {
                // start the service based on the android version
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    Toast.makeText(this, "start download", Toast.LENGTH_SHORT).show()
                    val serviceIntent = Intent(this, MyDownloadService::class.java)
                    ContextCompat.startForegroundService(this, serviceIntent)
                    startForegroundService(Intent(this, Musicplay::class.java))
                } else {
                    Toast.makeText(this, "start download else wala", Toast.LENGTH_SHORT).show()
                    val serviceIntent = Intent(this, MyDownloadService::class.java)
                    ContextCompat.startForegroundService(this, serviceIntent)
                }
            }
        } else {
            Toast.makeText(this, "start service download else wala", Toast.LENGTH_SHORT).show()
            startService(Intent(this, MyDownloadService::class.java))
        }
    }



    // method to ask user to grant the Overlay permission
    fun checkOverlayPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(this)) {
                // send user to the device settings
                val myIntent: Intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION)
                startActivity(myIntent)
            }
        }
    }


    override fun onResume() {
        super.onResume()
        Log.d("Resume Activity","Activity resume")
        overlayView.hideOverlay()
    }

    override fun onPause() {
//        checkOverlayPermission()
        Log.d("PauseActivity" ,"Activty Pause you outside this app")
        overlayView.showOverlay()
        super.onPause()
    }
    override fun onStop() {
        super.onStop()

        overlayView.showOverlay()
        Log.d("StopActivity" ,"Activty stop you outside this app")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("DestroyActivity" ,"Activty Destroy you outside this app")
        unregisterReceiver(receiver)
    }
}