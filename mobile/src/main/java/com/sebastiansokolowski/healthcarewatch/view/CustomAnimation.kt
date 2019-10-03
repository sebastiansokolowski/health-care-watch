package com.sebastiansokolowski.healthcarewatch.view

import android.content.Context
import android.graphics.drawable.AnimatedVectorDrawable
import android.support.graphics.drawable.AnimatedVectorDrawableCompat
import android.util.AttributeSet
import android.view.View
import android.widget.ImageView

/**
 * Created by Sebastian Sokołowski on 22.06.19.
 */
class CustomAnimation : ImageView {
    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    init {
        stopAnimation()
    }

    private fun changeAnimationState(animate: Boolean) {
        if (drawable is AnimatedVectorDrawable) {
            val animation = drawable as AnimatedVectorDrawable
            if (animate) {
                animation.start()
            } else {
                animation.stop()
            }
        } else if (drawable is AnimatedVectorDrawableCompat) {
            val animation = drawable as AnimatedVectorDrawableCompat
            if (animate) {
                animation.start()
            } else {
                animation.stop()
            }
        }
    }

    fun startAnimation() {
        visibility = View.VISIBLE
        changeAnimationState(true)
    }

    fun stopAnimation() {
        visibility = View.INVISIBLE
        changeAnimationState(false)
    }
}