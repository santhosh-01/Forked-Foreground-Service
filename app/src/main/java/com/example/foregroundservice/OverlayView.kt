package com.example.foregroundservice

import android.app.Activity
import android.app.Application
import android.content.Context
import android.graphics.PixelFormat
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.ProgressBar
import android.widget.TextView

class OverlayView(private val context: Context) {
    private var windowManager: WindowManager? = null
    private var overlayView: View? = null
    private var progressBar: ProgressBar? = null
    private var progresstext : TextView? = null
    private var isOverlayVisible = false


    private val activityLifecycleCallbacks = object : Application.ActivityLifecycleCallbacks {
        override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}
        override fun onActivityStarted(activity: Activity) {}
        override fun onActivityResumed(activity: Activity) {
            if (!isOverlayVisible) {
                hideOverlay()
            }
        }

        override fun onActivityPaused(activity: Activity) {
            if (isOverlayVisible) {
                showOverlay()
            }
        }

        override fun onActivityStopped(activity: Activity) {}
        override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
        override fun onActivityDestroyed(activity: Activity) {}
    }


    init {
        // Initialize WindowManager

        windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager

        // Inflate the overlay layout
        overlayView = LayoutInflater.from(context).inflate(R.layout.overlay_layout, null)

        // Initialize ProgressBar from overlay layout
        progressBar = overlayView?.findViewById(R.id.progress_Bar)
        progresstext = overlayView?.findViewById(R.id.text_progress)

        // Register activity lifecycle callbacks
        (context.applicationContext as Application).registerActivityLifecycleCallbacks(activityLifecycleCallbacks)
    }

    fun showOverlay() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val params = WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT
            )

            params.gravity = Gravity.TOP or Gravity.START
            try {
                windowManager?.addView(overlayView, params)
                isOverlayVisible = true
            }catch (e:Exception){
                e.printStackTrace()
                Log.d("OverlayView", "is not visible ${e.message}")
            }

        }
    }

    fun updateProgress(progress: Int) {
        if (isOverlayVisible) {
            progressBar?.progress = progress
            progresstext?.text = progress.toString()
        }
    }

    fun hideOverlay() {
        if (isOverlayVisible) {
//
//            progressBar!!.visibility = View.INVISIBLE
//            progresstext!!.visibility = View.INVISIBLE
            windowManager?.removeView(overlayView)
            isOverlayVisible = false
            Log.d("hideOverly","Hide view in overlay class")
        }else{
            Log.d("Not hideOverly","No Hide view in overlay class")
        }
    }

    fun onDestroy() {
        // Unregister activity lifecycle callbacks
        (context.applicationContext as Application).unregisterActivityLifecycleCallbacks(activityLifecycleCallbacks)
    }
}
