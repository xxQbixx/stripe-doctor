 package com.stripedoctor

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val prefs = getSharedPreferences("stripe", MODE_PRIVATE)

        // --- UI references ---
        val seekPosition   = findViewById<SeekBar>(R.id.seekPosition)
        val seekWidth      = findViewById<SeekBar>(R.id.seekWidth)
        val seekGreen      = findViewById<SeekBar>(R.id.seekGreen)
        val seekRed        = findViewById<SeekBar>(R.id.seekRed)
        val seekBlue       = findViewById<SeekBar>(R.id.seekBlue)
        val seekOpacity    = findViewById<SeekBar>(R.id.seekOpacity)
        val tvPosition     = findViewById<TextView>(R.id.tvPosition)
        val tvWidth        = findViewById<TextView>(R.id.tvWidth)
        val tvGreen        = findViewById<TextView>(R.id.tvGreen)
        val tvRed          = findViewById<TextView>(R.id.tvRed)
        val tvBlue         = findViewById<TextView>(R.id.tvBlue)
        val tvOpacity      = findViewById<TextView>(R.id.tvOpacity)
        val btnStart       = findViewById<Button>(R.id.btnStart)
        val btnStop        = findViewById<Button>(R.id.btnStop)
        val btnDiag        = findViewById<Button>(R.id.btnDiag)
        val btnSolid       = findViewById<Button>(R.id.btnSolid)
        val btnGrid        = findViewById<Button>(R.id.btnGrid)
        val btnBlack       = findViewById<Button>(R.id.btnBlack)
        val btnWhite       = findViewById<Button>(R.id.btnWhite)
        val btnRed         = findViewById<Button>(R.id.btnRed)
        val btnGreen       = findViewById<Button>(R.id.btnGreen)
        val tvStatus       = findViewById<TextView>(R.id.tvStatus)

        // Restore saved values
        seekPosition.max  = 1440
        seekWidth.max     = 20
        seekPosition.progress  = prefs.getInt("position", 720)
        seekWidth.progress     = prefs.getInt("width", 1)
        seekGreen.progress     = prefs.getInt("green", 255)
        seekRed.progress       = prefs.getInt("red", 0)
        seekBlue.progress      = prefs.getInt("blue", 0)
        seekOpacity.progress   = prefs.getInt("opacity", 128)

        fun updateLabels() {
            tvPosition.text = "Column position: ${seekPosition.progress}px"
            tvWidth.text    = "Stripe width: ${seekWidth.progress}px"
            tvGreen.text    = "Green subtract: ${seekGreen.progress}"
            tvRed.text      = "Red subtract: ${seekRed.progress}"
            tvBlue.text     = "Blue subtract: ${seekBlue.progress}"
            tvOpacity.text  = "Overlay opacity: ${seekOpacity.progress}"
        }
        updateLabels()

        val seekListener = object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(s: SeekBar, v: Int, u: Boolean) {
                updateLabels()
                // Live update the overlay if running
                sendCurrentConfig()
            }
            override fun onStartTrackingTouch(s: SeekBar) {}
            override fun onStopTrackingTouch(s: SeekBar) {
                prefs.edit()
                    .putInt("position", seekPosition.progress)
                    .putInt("width",    seekWidth.progress)
                    .putInt("green",    seekGreen.progress)
                    .putInt("red",      seekRed.progress)
                    .putInt("blue",     seekBlue.progress)
                    .putInt("opacity",  seekOpacity.progress)
                    .apply()
            }
        }
        seekPosition.setOnSeekBarChangeListener(seekListener)
        seekWidth.setOnSeekBarChangeListener(seekListener)
        seekGreen.setOnSeekBarChangeListener(seekListener)
        seekRed.setOnSeekBarChangeListener(seekListener)
        seekBlue.setOnSeekBarChangeListener(seekListener)
        seekOpacity.setOnSeekBarChangeListener(seekListener)

        btnStart.setOnClickListener {
            if (!Settings.canDrawOverlays(this)) {
                tvStatus.text = "⚠ Grant overlay permission first!"
                startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:$packageName")))
                return@setOnClickListener
            }
            sendCurrentConfig()
            startForegroundService(Intent(this, OverlayService::class.java).apply {
                action = OverlayService.ACTION_START
            })
            tvStatus.text = "✓ Stripe correction ACTIVE"
        }

        btnStop.setOnClickListener {
            startService(Intent(this, OverlayService::class.java).apply {
                action = OverlayService.ACTION_STOP
            })
            tvStatus.text = "○ Overlay stopped"
        }

        // Debug screens — launch full-screen solid color activities
        btnDiag.setOnClickListener  { startActivity(Intent(this, DiagActivity::class.java).putExtra("mode", "diag")) }
        btnSolid.setOnClickListener { startActivity(Intent(this, DiagActivity::class.java).putExtra("mode", "solid")) }
        btnGrid.setOnClickListener  { startActivity(Intent(this, DiagActivity::class.java).putExtra("mode", "grid")) }
        btnBlack.setOnClickListener { startActivity(Intent(this, DiagActivity::class.java).putExtra("mode", "black")) }
        btnWhite.setOnClickListener { startActivity(Intent(this, DiagActivity::class.java).putExtra("mode", "white")) }
        btnRed.setOnClickListener   { startActivity(Intent(this, DiagActivity::class.java).putExtra("mode", "red")) }
        btnGreen.setOnClickListener { startActivity(Intent(this, DiagActivity::class.java).putExtra("mode", "green")) }
    }

    private fun sendCurrentConfig() {
        val prefs = getSharedPreferences("stripe", MODE_PRIVATE)
        startService(Intent(this, OverlayService::class.java).apply {
            action = OverlayService.ACTION_UPDATE
            putExtra("position", prefs.getInt("position", 720))
            putExtra("width",    prefs.getInt("width", 1))
            putExtra("green",    prefs.getInt("green", 255))
            putExtra("red",      prefs.getInt("red", 0))
            putExtra("blue",     prefs.getInt("blue", 0))
            putExtra("opacity",  prefs.getInt("opacity", 128))
        })
    }
}
