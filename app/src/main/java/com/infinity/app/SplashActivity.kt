package com.infinity.app

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.LinearInterpolator
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity

class SplashActivity : AppCompatActivity() {

    private val handler = Handler(Looper.getMainLooper())
    private var pulseAnimator: AnimatorSet? = null

    private val openApp = Runnable {
        startActivity(Intent(this, MainActivity::class.java))
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        finish()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.statusBarColor = android.graphics.Color.BLACK
        window.navigationBarColor = android.graphics.Color.BLACK
        window.decorView.systemUiVisibility = (
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        )

        setContentView(R.layout.activity_splash)

        val logo = findViewById<ImageView>(R.id.splashLogo)
        logo.alpha = 0f
        logo.scaleX = 0.82f
        logo.scaleY = 0.82f

        val appear = AnimatorSet().apply {
            playTogether(
                ObjectAnimator.ofFloat(logo, View.ALPHA, 0f, 1f),
                ObjectAnimator.ofFloat(logo, View.SCALE_X, 0.82f, 1f),
                ObjectAnimator.ofFloat(logo, View.SCALE_Y, 0.82f, 1f)
            )
            duration = 700L
            interpolator = AccelerateDecelerateInterpolator()
        }

        val pulseX = PropertyValuesHolder.ofFloat(
            View.SCALE_X, 1f, 1.045f, 1f
        )
        val pulseY = PropertyValuesHolder.ofFloat(
            View.SCALE_Y, 1f, 1.045f, 1f
        )

        pulseAnimator = AnimatorSet().apply {
            val pulse = ObjectAnimator.ofPropertyValuesHolder(logo, pulseX, pulseY)
            pulse.duration = 1400L
            pulse.repeatCount = ObjectAnimator.INFINITE
            pulse.repeatMode = ObjectAnimator.RESTART
            pulse.interpolator = LinearInterpolator()
            play(pulse)
        }

        appear.addListener(object : android.animation.AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: android.animation.Animator) {
                if (!isFinishing) pulseAnimator?.start()
            }
        })

        appear.start()
        handler.postDelayed(openApp, 2600L)
    }

    override fun onDestroy() {
        handler.removeCallbacksAndMessages(null)
        pulseAnimator?.cancel()
        pulseAnimator = null
        super.onDestroy()
    }
}
