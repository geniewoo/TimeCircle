package com.example.user.timecircle

import android.graphics.Color
import android.graphics.Point
import android.os.Bundle
import android.support.v4.app.Fragment
import android.transition.Scene
import android.transition.TransitionManager
import android.util.Log.i
import android.view.*
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.FrameLayout
import android.widget.LinearLayout
import org.jetbrains.anko.*
import org.jetbrains.anko.sdk25.coroutines.onClick
import org.jetbrains.anko.support.v4.UI
import org.jetbrains.anko.support.v4.dimen
import kotlin.math.atan2

/**
 * Created by SungWoo on 2018-08-06.
 */
class TimeCircleFragment : Fragment() {
    var centerX = 0
    var centerY = 0
    private val circleViews = arrayOfNulls<CircleView>(CIRCLE_NUM)
    private val scale1 = 1.0f
    private val scale2 = 4.0f
    private val position1 = 0.0f
    private val position2 = -1000.0f
    private val duration: Long = 500
    private lateinit var circleFrameLayout: FrameLayout
    var animated = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val ui = UI {
            val rootLayout = verticalLayout {
                circleFrameLayout = frameLayout {
                    imageView {
                        imageResource = R.drawable.circle_image
                        z = 2.0f
                    }.lparams (wrapContent, wrapContent) {
                        gravity = Gravity.CENTER
                    }
                    imageView {
                        imageResource = R.drawable.circle_stroke_image
                        z = 2.0f
                    }.lparams (wrapContent, wrapContent) {
                        gravity = Gravity.CENTER
                    }
                }.lparams(dimen(R.dimen.timeCircle_Length), dimen(R.dimen.timeCircle_Length)) {
                    gravity = Gravity.CENTER
                }
                circleFrameLayout.setOnTouchListener(onTouchListener)
                for (i in 0 until CIRCLE_NUM) {
                    val circleView = CircleView(context, i)
                    circleView.z = 1.0f
                    circleFrameLayout.addView(circleView)
                    circleViews[i] = circleView
                }
            }
            rootLayout.onClick { v -> v?.let { translation(it) } }
        }
        val returnView = ui.view
        setCenterX_Y()
        return returnView
    }

    private fun translation(view: View) {
        if (animated) {
            circleFrameLayout.animate().scaleX(scale1).scaleY(scale1).y(position1).setDuration(duration).start()
            animated = false
        } else {
            circleFrameLayout.animate().scaleX(scale2).scaleY(scale2).y(position2).setDuration(duration).start()
            animated = true
        }
    }

    private fun setCenterX_Y() {
        val size = Point()
        centerX = dimen(R.dimen.timeCircle_Length) / 2
        centerY = dimen(R.dimen.timeCircle_Length) / 2
    }

    private val onTouchListener: View.OnTouchListener = View.OnTouchListener { view: View?, motionEvent: MotionEvent ->
        val x = centerX - motionEvent.x
        val y = centerY - motionEvent.y

        val length = double(x) + double(y)
        if (length < double(dimen(R.dimen.timeCircle_Length) / 2) && length > double(dimen(R.dimen.timeImage_Length) / 2)) {
            val circleIndex = (((atan2(x, y) * (180 / Math.PI) + 450) % 360) / -UNIT_ANGLE).toInt()
            circleViews[circleIndex]?.apply {
                mColor = Color.GREEN
                invalidate()
            }
        }
        true
    }

    private fun double(mono: Float): Float = mono * mono
    private fun double(mono: Int): Float = (mono * mono).toFloat()
}
