package com.stripedoctor

import android.graphics.*
import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AppCompatActivity

/**
 * Full-screen diagnostic canvas for visual fault mapping.
 *
 * Modes:
 *   black  — pure black, shows any light leakage / always-on pixels clearly
 *   white  — pure white, maximum stress test, thermal + electrical
 *   red    — isolates R channel, see if stripe has R component
 *   green  — isolates G channel, confirm stripe is purely G
 *   diag   — diagonal gradient, reveals column/row boundary artifacts
 *   solid  — mid-grey, best for seeing subtle luminance offsets
 *   grid   — 1px grid, pixel-perfect alignment diagnostics
 */
class DiagActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // True full screen — hide everything
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        window.decorView.systemUiVisibility = (
            View.SYSTEM_UI_FLAG_FULLSCREEN or
            View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
            View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        )

        val mode = intent.getStringExtra("mode") ?: "black"
        setContentView(DiagView(this, mode))
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        finish()
        return true
    }
}

class DiagView(context: android.content.Context, private val mode: String)
    : View(context) {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)

    override fun onDraw(canvas: Canvas) {
        val w = width.toFloat()
        val h = height.toFloat()

        when (mode) {
            "black" -> canvas.drawColor(Color.BLACK)

            "white" -> canvas.drawColor(Color.WHITE)

            "red"   -> canvas.drawColor(Color.RED)

            "green" -> canvas.drawColor(Color.GREEN)

            "solid" -> canvas.drawColor(Color.rgb(128, 128, 128))

            "diag"  -> {
                // Diagonal gradient — exposes column driver boundary artifacts
                val shader = LinearGradient(
                    0f, 0f, w, h,
                    intArrayOf(
                        Color.BLACK,
                        Color.RED,
                        Color.GREEN,
                        Color.BLUE,
                        Color.WHITE
                    ),
                    null,
                    Shader.TileMode.CLAMP
                )
                paint.shader = shader
                canvas.drawRect(0f, 0f, w, h, paint)
                paint.shader = null

                // Overlay stripe position markers every 100px
                paint.color = Color.argb(80, 255, 255, 255)
                paint.strokeWidth = 1f
                var x = 0f
                while (x < w) {
                    canvas.drawLine(x, 0f, x, h, paint)
                    x += 100f
                }
                // Label every 100px column
                paint.color = Color.WHITE
                paint.textSize = 24f
                paint.shader = null
                x = 0f
                while (x < w) {
                    canvas.drawText("${x.toInt()}", x + 2f, 30f, paint)
                    x += 100f
                }
            }

            "grid"  -> {
                canvas.drawColor(Color.BLACK)
                paint.color       = Color.WHITE
                paint.strokeWidth = 1f

                // Vertical lines every pixel (shows column isolation)
                var x = 0f
                while (x < w) {
                    canvas.drawLine(x, 0f, x, h, paint)
                    x += 2f  // every other pixel — classic checkerboard column test
                }
                // Horizontal lines
                var y = 0f
                while (y < h) {
                    canvas.drawLine(0f, y, w, y, paint)
                    y += 2f
                }

                // Column position labels every 60px
                paint.textSize = 20f
                paint.color    = Color.YELLOW
                x = 0f
                while (x < w) {
                    canvas.drawText("${x.toInt()}", x, h / 2f, paint)
                    x += 60f
                }
            }
        }
    }
}
