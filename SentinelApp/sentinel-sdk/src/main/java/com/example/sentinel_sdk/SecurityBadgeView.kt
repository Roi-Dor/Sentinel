package com.example.sentinel_sdk

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView

class SecurityBadgeView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : AppCompatImageView(context, attrs) {

    init {
        setImageResource(R.drawable.ic_shield) // Make sure this drawable exists
        setColorFilter(Color.GRAY) // Default state
    }

    fun setScore(score: Int) {
        val color = when {
            score >= 80 -> Color.parseColor("#4CAF50") // Green
            score >= 50 -> Color.parseColor("#FFC107") // Yellow
            else -> Color.parseColor("#F44336") // Red
        }
        setColorFilter(color)
    }
}