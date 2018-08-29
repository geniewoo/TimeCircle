package com.example.user.timecircle

import android.graphics.Color
import android.graphics.Point
import android.os.Bundle
import android.support.v4.app.Fragment
import android.transition.Scene
import android.transition.TransitionManager
import android.util.Log.i
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.FrameLayout
import android.widget.LinearLayout
import org.jetbrains.anko.find
import org.jetbrains.anko.include
import org.jetbrains.anko.sdk25.coroutines.onClick
import org.jetbrains.anko.support.v4.UI
import org.jetbrains.anko.support.v4.dimen
import org.jetbrains.anko.verticalLayout
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
    private lateinit var scene1: Scene
    private lateinit var scene2: Scene
    var animated = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val ui = UI {
            verticalLayout {
                id = View.generateViewId()
                val containerView = include<LinearLayout>(R.layout.time_circle_layout1)
                val circleLayout = containerView.find<FrameLayout>(R.id.time_circle_layout1_frame_layout)
                circleLayout.setOnTouchListener(onTouchListener)
                for (i in 0 until CIRCLE_NUM) {
                    val circleView = CircleView(context, i)
                    circleView.z = 1.0f
                    circleLayout.addView(circleView)
                    circleViews[i] = circleView
                }
            }
        }
        val returnView = ui.view
        setCenterX_Y()
        makeScenes(returnView as ViewGroup)
        returnView.onClick { v -> v?.let { translation(it as ViewGroup) } }
        return returnView
    }

    private fun translation(view: ViewGroup) {
//        TransitionManager.go(scene2)
//        TransitionManager.beginDelayedTransition(view)

        val circleLayout = view.find<FrameLayout>(R.id.time_circle_layout1_frame_layout)
//        TransitionManager.beginDelayedTransition(circleLayout)
//        circleLayout.layoutParams =
//                circleLayout.layoutParams.let {
//                    it.width = matchParent
//                    it.height = matchParent
//                    it
//                }
//        val animation: Animation = AnimationUtils.loadAnimation(context, R.anim.circle_view_zoom)
//        circleLayout.startAnimation(animation)
//        circleLayout.setScaleX(2.0f)
//        circleLayout.setScaleY(2.0f)
        if (animated) {
            circleLayout.animate().scaleX(scale1).scaleY(scale1).y(position1).setDuration(duration).start()
            animated = false
        } else {
            circleLayout.animate().scaleX(scale2).scaleY(scale2).y(position2).setDuration(duration).start()
            animated = true
        }
    }

    private fun makeScenes(rootView: ViewGroup) {
        scene1 = Scene.getSceneForLayout(rootView, R.layout.time_circle_layout1, context)
        scene2 = Scene.getSceneForLayout(rootView, R.layout.time_circle_layout2, context)
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
            i("coco ", "atan : " + (atan2(x, y) * (180 / Math.PI)).toString())
            val circleIndex = (((atan2(x, y) * (180 / Math.PI) + 450) % 360) / -UNIT_ANGLE).toInt()
            i("coconum", circleIndex.toString())
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
