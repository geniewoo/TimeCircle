package com.example.user.timecircle

import android.widget.FrameLayout
import com.example.user.timecircle.common.CIRCLE_NUM
import com.example.user.timecircle.common.CommonUtil.convertToCircleIndex
import kotlinx.android.synthetic.main.time_circle_fragment.view.*

private const val ACTIVITY_DRAG_DEFAULT_NUM = 3
private val DEFAULT_COLOR = ActivityColor.COLOR4.colorRes

class CircleViewsController(layout: FrameLayout) {

    var activityLastDragIndex: Int? = null

    private val circleViews = ArrayList<CircleView>()

    init {
        for (i in 0 until CIRCLE_NUM) {
            val circleView = CircleView(layout.context)
            circleView.z = 1.0f
            layout.time_circle_inner_frame_layout.addView(circleView)
            circleViews.add(circleView)
        }
    }

    fun changeColor(color: ActivityColor, index: Int) {
        circleViews[index.convertToCircleIndex()].changeColor(color.colorRes)
    }

    fun changeColorForActivityDrag(color: ActivityColor, index: Int, touchFinish: Boolean) {
        for (i in -1 until ACTIVITY_DRAG_DEFAULT_NUM - 1) {
            activityLastDragIndex?.let {
                circleViews[(it + i).convertToCircleIndex()].changeColor(DEFAULT_COLOR)
            }
            circleViews[(index + i).convertToCircleIndex()].changeColor(color.colorRes)
        }
        activityLastDragIndex = if (touchFinish) null else index
    }

    fun removeColorForActivityDrag() {
        for (i in -1 until ACTIVITY_DRAG_DEFAULT_NUM - 1) {
            activityLastDragIndex?.let {
                circleViews[(it + i).convertToCircleIndex()].changeColor(DEFAULT_COLOR)
            }
        }
        activityLastDragIndex = null
    }
}

enum class ActivityColor(val colorRes: Int) {
    COLOR1(R.color.yellow),
    COLOR2(R.color.green),
    COLOR3(R.color.red),
    COLOR4(R.color.blue),
    COLOR_TRANSPARENT(R.color.transparent)
    ;
}
