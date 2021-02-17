package com.example.user.timecircle

import android.widget.FrameLayout
import com.example.user.timecircle.common.CIRCLE_NUM
import com.example.user.timecircle.common.CommonUtil.convertToCircleIndex
import kotlinx.android.synthetic.main.time_circle_fragment.view.*

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

    private fun changeColor(component: ActivityComponent, index: Int) {
        index.convertToCircleIndex().takeIf { !activitySetManager.isActivitySetExist(it) }?.let {
            circleViews[it].changeColor(component.colorRes)
        }
    }

    fun changeColorForActivityDrop(component: ActivityComponent, index: Int, dropConfirm: Boolean): Boolean {
        if (activitySetManager.isActivitySetExist(index)) {
            removeColorForActivityDrop()
            return false
        }
        val tempActivitySet = activitySetManager.makeActivitySet(index, component)
        removeColorForDrop()
        changeColorForDrop(component, tempActivitySet)
        lastDropActivitySet = if (dropConfirm) {
            addActivitySet(index, component)
            null
        } else {
            tempActivitySet
        }
        return true
    }

    private fun addActivitySet(index: Int, component: ActivityComponent) {
        val activitySet = activitySetManager.makeActivitySet(index, component)
        activitySetManager.insertActivitySet(activitySet)
    }

    fun removeColorForActivityDrop() {
        removeColorForDrop()
        lastDropActivitySet = null
    }

    private fun changeColorForDrop(component: ActivityComponent, activitySet: ActivitySet) {
        for (i in activitySet.fromIndex..activitySet.toIndex) {
            changeColor(component, i)
        }
    }

    private fun removeColorForDrop() {
        lastDropActivitySet?.run {
            for (i in fromIndex..toIndex) {
                changeColor(ActivityComponent.ComponentTransparent, i)
            }
        }
    }

    fun returnIndexEdgeOfActivitySet(index: Int): TouchMode.AdjustActivity? = activitySetManager.returnIndexEdgeOfActivitySet(index)

    fun adjustActivity(touchedIndex: Int, adjustActivity: TouchMode.AdjustActivity) {
        activitySetManager.adjustAvailableIndex(touchedIndex, adjustActivity) {
            fromIndex, toIndex, color ->
            for (i in fromIndex..toIndex) {
                circleViews[i].changeColor(color.colorRes)
            }
        }
    }

    fun adjustActivityDone(adjustActivity: TouchMode.AdjustActivity) {
        activitySetManager.adjustActivityDone(adjustActivity)
    }
}

enum class ActivityComponent(val colorRes: Int) {
    Component1(R.color.yellow),
    Component2(R.color.green),
    Component3(R.color.red),
    Component4(R.color.blue),
    Component5(R.color.pink),
    ComponentTransparent(R.color.transparent)
    ;
}