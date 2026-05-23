package id.biz.rrndev.appanalyzer.util

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import kotlin.math.roundToInt

object DrawableTools {
    fun Drawable.toAppIconBitmap(density: Float): Bitmap? {
        return runCatching {
            val size = (48f * density).roundToInt().coerceAtLeast(48)
            if (this is BitmapDrawable && bitmap != null) {
                Bitmap.createScaledBitmap(bitmap, size, size, true)
            } else {
                val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
                val canvas = Canvas(bitmap)
                setBounds(0, 0, canvas.width, canvas.height)
                draw(canvas)
                bitmap
            }
        }.getOrNull()
    }
}

