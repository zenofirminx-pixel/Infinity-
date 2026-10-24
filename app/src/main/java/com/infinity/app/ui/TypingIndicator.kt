package com.infinity.app.ui

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Shader
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.View
import android.view.animation.Animation
import android.view.animation.ScaleAnimation
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.setPadding
import com.infinity.app.R

/** Native "Infinity réfléchit…" indicator: animated white dot, fixed text, light sweep. */
object TypingIndicator {
    fun create(context: Context): View {
        val density = context.resources.displayMetrics.density
        return LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding((2 * density).toInt())
            val dot = View(context).apply {
                layoutParams = LinearLayout.LayoutParams((7 * density).toInt(), (7 * density).toInt()).apply {
                    marginEnd = (9 * density).toInt()
                }
                background = GradientDrawable().apply {
                    shape = GradientDrawable.OVAL
                    setColor(Color.WHITE)
                }
                startAnimation(ScaleAnimation(0.65f, 1.25f, 0.65f, 1.25f,
                    Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f).apply {
                    duration = 700
                    repeatMode = Animation.REVERSE
                    repeatCount = Animation.INFINITE
                })
            }
            addView(dot)
            addView(ShimmerLabel(context).apply {
                text = "Infinity réfléchit…"
                textSize = 14f
                setTextColor(context.getColor(R.color.infinity_muted))
                contentDescription = "Infinity réfléchit"
                layoutParams = LinearLayout.LayoutParams(-2, -2)
            })
        }
    }

    private class ShimmerLabel(context: Context) : TextView(context) {
        private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        private var shift = 0f
        private val animator = ValueAnimator.ofFloat(-1f, 2f).apply {
            duration = 1450
            repeatCount = ValueAnimator.INFINITE
            addUpdateListener { animation ->
                shift = animation.animatedValue as Float
                invalidate()
            }
        }
        override fun onAttachedToWindow() { super.onAttachedToWindow(); animator.start() }
        override fun onDetachedFromWindow() { animator.cancel(); paint.shader = null; super.onDetachedFromWindow() }
        override fun onDraw(canvas: Canvas) {
            val base = currentTextColor
            val w = width.coerceAtLeast(1).toFloat()
            paint.shader = LinearGradient(
                shift * w - w, 0f, shift * w, 0f,
                intArrayOf(base, Color.WHITE, base), floatArrayOf(0.35f, 0.5f, 0.65f), Shader.TileMode.CLAMP
            )
            paint.textSize = textSize
            paint.typeface = typeface
            paint.textAlign = Paint.Align.LEFT
            paint.isSubpixelText = true
            val baseline = (height - paint.fontMetrics.bottom - paint.fontMetrics.top) / 2f
            canvas.drawText(text?.toString().orEmpty(), compoundPaddingLeft.toFloat(), baseline, paint)
        }
    }
}
