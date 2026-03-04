package com.stripedoctor

import android.app.*
import android.content.Intent
import android.graphics.*
import android.os.Build
import android.os.IBinder
import android.view.View
import android.view.WindowManager

class OverlayService : Service() {

    companion object {
        const val ACTION_START  = "START"
        const val ACTION_STOP   = "STOP"
        const val ACTION_UPDATE = "UPDATE"
        const val CHANNEL_ID    = "stripe_doctor"
    }

    private var overlayView: StripeOverlayView? = null
    private lateinit var wm: WindowManager

    override fun onCreate() {
        super.onCreate()
        wm = getSystemService(WINDOW_SERVICE) as WindowManager
        createNotificationChannel()
        startForeground(1, buildNotification())
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START, ACTION_UPDATE -> {
                val position = intent.getIntExtra("position", 720)
                val width    = intent.getIntExtra("width", 1)
                val green    = intent.getIntExtra("green", 255)
                val red      = intent.getIntExtra("red", 0)
                val blue     = intent.getIntExtra("blue", 0)
                val opacity  = intent.getIntExtra("opacity", 128)

                if (overlayView == null) {
                    overlayView = StripeOverlayView(this)
                    val params = WindowManager.LayoutParams(
                        WindowManager.LayoutParams.MATCH_PARENT,
                        WindowManager.LayoutParams.MATCH_PARENT,
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                        else
                            WindowManager.LayoutParams.TYPE_PHONE,
                        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                        WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
                        WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                        WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                        PixelFormat.TRANSLUCENT
                    )
                    wm.addView(overlayView, params)
                }
                overlayView?.setConfig(position, width, green, red, blue, opacity)
            }
            ACTION_STOP -> {
                overlayView?.let { wm.removeView(it) }
                overlayView = null
                stopSelf()
            }
        }
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel(CHANNEL_ID, "Stripe Doctor", NotificationManager.IMPORTANCE_LOW)
                .also { (getSystemService(NOTIFICATION_SERVICE) as NotificationManager).createNotificationChannel(it) }
        }
    }

    private fun buildNotification(): Notification {
        val pi = PendingIntent.getActivity(this, 0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE)
        return (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            Notification.Builder(this, CHANNEL_ID)
        else
            @Suppress("DEPRECATION") Notification.Builder(this))
            .setContentTitle("Stripe Doctor active")
            .setContentText("Display correction overlay running")
            .setSmallIcon(android.R.drawable.ic_menu_view)
            .setContentIntent(pi)
            .build()
    }
}
