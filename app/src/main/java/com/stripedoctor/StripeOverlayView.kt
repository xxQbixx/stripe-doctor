package com.stripedoctor

import android.content.Context
import android.graphics.*
import android.view.View

/**
 * A full-screen transparent overlay View that renders a single vertical
 * correction stripe using Porter-Duff compositing math.
 *
 * The stripe subtracts the parasitic channel values from the underlying
 * display output — essentially a software anti-toxin for the column driver
 * voltage offset.
 *
 * Mode:
 *   - We draw a vertical rectangle at [position, 0] → [position+width, screenHeight]
 *   - The color is constructed from the subtract values with the given opacity
 *   - Paint uses PorterDuff.Mode.MULTIPLY to suppress the offending channel
 *     (multiply by <1.0 on the green axis = subtract green energy)
 */
class StripeOverlayView(context: Context) : View(context) {

    private var stripePosition = 720
    private var stripeWidth    = 1
    private var greenSubtract  = 255   // 0-255 how much green to kill
    private var redSubtract    = 0
    private var blueSubtract   = 0
    private var overlayOpacity = 128

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val rect  = RectF()

    fun setConfig(position: Int, width: Int, green: Int, red: Int, blue: Int, opacity: Int) {
        stripePosition = position
        stripeWidth    = width
        greenSubtract  = green
        redSubtract    = red
        blueSubtract   = blue
        overlayOpacity = opacity
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // Convert subtract values (0-255) into MULTIPLY color components
        // MULTIPLY mode: result = dst * src/255
        // To SUBTRACT green: src green = 0 (multiply by 0 kills green)
        // To leave red/blue untouched: src = 255 (multiply by 1.0 = no change)
        val r = (255 - redSubtract).coerceIn(0, 255)
        val g = (255 - greenSubtract).coerceIn(0, 255)
        val b = (255 - blueSubtract).coerceIn(0, 255)

        paint.color  = Color.argb(overlayOpacity, r, g, b)
        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.MULTIPLY)

        rect.set(
            stripePosition.toFloat(),
            0f,
            (stripePosition + stripeWidth).toFloat(),
            height.toFloat()
        )
        canvas.drawRect(rect, paint)
    }
}
