package com.infinity.app.ui

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Color
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator

/** Un seul point animé, indépendant de l'adaptateur de messages. */
class TypingIndicatorView(context: Context) : View(context) {
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.WHITE }
    private val density = resources.displayMetrics.density
    private var radius = 4.5f * density
    private val pulse = ValueAnimator.ofFloat(3.2f * density, 6.2f * density).apply {
        duration = 720L
        repeatMode = ValueAnimator.REVERSE
        repeatCount = ValueAnimator.INFINITE
        interpolator = AccelerateDecelerateInterpolator()
        addUpdateListener { radius = it.animatedValue as Float; invalidate() }
    }
    override fun onAttachedToWindow() { super.onAttachedToWindow(); if (!pulse.isStarted) pulse.start() }
    override fun onDetachedFromWindow() { pulse.cancel(); super.onDetachedFromWindow() }
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val size = (28 * density).toInt()
        setMeasuredDimension(resolveSize(size, widthMeasureSpec), resolveSize(size, heightMeasureSpec))
    }
    override fun onDraw(canvas: Canvas) { super.onDraw(canvas); canvas.drawCircle(width / 2f, height / 2f, radius, paint) }
}
