package com.example.user.timecircle

import android.widget.FrameLayout
import com.example.user.timecircle.common.CIRCLE_NUM
import com.example.user.timecircle.common.CommonUtil.convertToCircleIndex
import kotlinx.android.synthetic.main.time_circle_fragment.view.*

private val DEFAULT_COLOR = ActivityColor.COLOR4

class CircleViewsController(layout: FrameLayout) {

    var lastDropActivitySet: ActivitySet? = null

    private val circleViews = ArrayList<CircleView>()
    private val activitySetManager = ActivitySetManager()

    init {
        for (i in 0 until CIRCLE_NUM) {
            val circleView = CircleView(layout.context)
            circleView.z = 1.0f
            layout.time_circle_inner_frame_layout.addView(circleView)
            circleViews.add(circleView)
        }
    }

    private fun changeColor(color: ActivityColor, index: Int) {
        index.convertToCircleIndex().takeIf { !activitySetManager.isActivitySetExist(it) }?.let {
            circleViews[it].changeColor(color.colorRes)
        }
    }

    fun changeColorForActivityDrop(color: ActivityColor, index: Int, dropConfirm: Boolean): Boolean {
        if (activitySetManager.isActivitySetExist(index)) {
            removeColorForActivityDrop()
            return false
        }
        val tempActivitySet = activitySetManager.makeActivitySet(index)
        removeColorForDrop()
        changeColorForDrop(color, tempActivitySet)
        lastDropActivitySet = if (dropConfirm) {
            addActivitySet(index)
            null
        } else {
            tempActivitySet
        }
        return true
    }

    private fun addActivitySet(index: Int) {
        val activitySet = activitySetManager.makeActivitySet(index)
        activitySetManager.insertActivitySet(activitySet)
    }

    fun removeColorForActivityDrop() {
        removeColorForDrop()
        lastDropActivitySet = null
    }

    private fun changeColorForDrop(color: ActivityColor, activitySet: ActivitySet) {
        for (i in activitySet.fromIndex..activitySet.toIndex) {
            changeColor(color, i)
        }
    }

    private fun removeColorForDrop() {
        lastDropActivitySet?.run {
            for (i in fromIndex..toIndex) {
                changeColor(DEFAULT_COLOR, i)
            }
        }
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
