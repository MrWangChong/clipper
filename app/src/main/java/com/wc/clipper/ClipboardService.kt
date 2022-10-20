package com.wc.clipper

import android.app.*
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import android.util.Log
import android.view.Gravity
import android.view.WindowManager
import android.widget.FrameLayout

class ClipboardService : Service() {

    override fun onCreate() {
        super.onCreate()
        Log.d(ClipperReceiver.TAG, "Start clipboard service.")
        val clipboardService = ClipperReceiver()
        val intentFilter = IntentFilter()
        intentFilter.addAction("clipper.set")
        intentFilter.addAction("clipper.get")
        registerReceiver(clipboardService, intentFilter)
        showFloat()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val pendingIntent: PendingIntent =
                Intent(this, MainActivity::class.java).let { notificationIntent ->
                    PendingIntent.getActivity(
                        this, 0, notificationIntent,
                        PendingIntent.FLAG_IMMUTABLE
                    )
                }

            (getSystemService(NOTIFICATION_SERVICE) as? NotificationManager)?.let {
                val channel = NotificationChannel(
                    CHANNEL_DEFAULT_IMPORTANCE,
                    getText(R.string.app_name),
                    NotificationManager.IMPORTANCE_LOW
                )
                it.createNotificationChannel(channel)
            }
            val notification: Notification = Notification.Builder(this, CHANNEL_DEFAULT_IMPORTANCE)
                .setContentTitle(getText(R.string.app_name))
                .setContentText("剪切板服务")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentIntent(pendingIntent)
                .setTicker("剪切板服务")
                .build()
            startForeground(ONGOING_NOTIFICATION_ID, notification)
        }
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            stopForeground(true)
        }
        super.onDestroy()
    }

    private fun showFloat() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
            && !Settings.canDrawOverlays(applicationContext)
        ) {
            return
        }
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        ).apply {
            type =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                else WindowManager.LayoutParams.TYPE_PHONE
            format = PixelFormat.TRANSPARENT
            gravity = Gravity.END or Gravity.TOP
            flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        }
        val windowView = FrameLayout(applicationContext)
        windowView.requestFocus()
        windowView.setBackgroundColor(Color.BLACK)
        val wm = getSystemService(Activity.WINDOW_SERVICE) as WindowManager
//        windowView.setOnClickListener {
//            wm.removeViewImmediate(windowView)
//        }
        params.width = 1
        params.height = 1
        params.x = 100
        params.y = 100
        wm.addView(windowView, params)
    }

    companion object {
        private const val ONGOING_NOTIFICATION_ID = 1
        private const val CHANNEL_DEFAULT_IMPORTANCE = "ClipboardService"
    }
}