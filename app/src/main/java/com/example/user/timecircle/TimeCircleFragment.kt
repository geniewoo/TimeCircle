package com.example.user.timecircle

import android.graphics.Color
import android.graphics.Color.GREEN
import android.graphics.Point
import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log.i
import android.view.*
import android.widget.FrameLayout
import org.jetbrains.anko.*
import org.jetbrains.anko.support.v4.UI
import org.jetbrains.anko.support.v4.dimen
import kotlin.math.atan2

/**
 * Created by SungWoo on 2018-08-06.
 */
class TimeCircleFragment : Fragment() {
    lateinit var circleLayout: FrameLayout
    var centerX: Int = 0
    var centerY: Int = 0
    val circleViews = arrayOfNulls<CircleView>(CIRCLE_NUM)
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val ui = UI {
            verticalLayout {
                circleLayout = frameLayout {
                    imageView {
                        background = context.getDrawable(R.drawable.circle_image)
                        z = 2.0f
                    }.lparams {
                        gravity = Gravity.CENTER
                    }
                    setOnTouchListener(onTouchListener)
                }.lparams(dimen(R.dimen.timeCircle_Length), dimen(R.dimen.timeCircle_Length)) {
                    gravity = Gravity.CENTER
                }
                for (i in 0 until CIRCLE_NUM - 4) {
                    val circleView = CircleView(context, i)
                    circleView.z = 1.0f
                    circleLayout.addView(circleView)
                    circleViews[i] = circleView
                }
                var title = editText {
                    hint = "test!!!!"
                }
            }
        }
        val returnView = ui.view
        setCenterX_Y()
        return returnView
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