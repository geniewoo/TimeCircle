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
            layout.time_circle_third_layer.addView(circleView)
            circleViews.add(circleView)
        }
    }

    private fun changeColor(component: ActivityComponent, index: Int) {
        index.convertToCircleIndex().takeIf { activitySetManager.findActivitySet(it) == null }?.let {
            circleViews[it].changeColor(component.colorRes)
        }
    }

    fun changeColorForActivityEdit(component: ActivityComponent, index: Int, dropConfirm: Boolean): Boolean {
        if (activitySetManager.findActivitySet(index) != null) {
            removeColorForActivityEdit()
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

    fun removeColorForActivityEdit() {
        removeColorForDrop()
        lastDropActivitySet = null
    }

    fun removeActivityForMove(activitySet: ActivitySet) {
        removeColorForDrop()
        lastDropActivitySet = null
        activitySetManager.removeActivity(activitySet)
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

    fun returnActivityUnconfirmedMode(index: Int): TouchMode? = activitySetManager.returnActivityUnconfirmedMode(index)

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

    fun changeColorForActivityMoveReady(activitySet: ActivitySet): Boolean {
        for (i in activitySet.fromIndex..activitySet.toIndex) {
            circleViews[i].changeColor(activitySet.component.tintColorRes)
        }
        return true
    }

    fun recoverColorForActivityMoveCancel(activitySet: ActivitySet) {
        for (i in activitySet.fromIndex..activitySet.toIndex) {
            circleViews[i].changeColor(activitySet.component.colorRes)
        }
    }

    fun removeColorForActivityMove(activitySet: ActivitySet) {
        for (i in activitySet.fromIndex..activitySet.toIndex) {
            circleViews[i].changeColor(ActivityComponent.ComponentTransparent.colorRes)
        }
    }
}

enum class ActivityComponent(val colorRes: Int, val tintColorRes: Int) {
    Component1(R.color.yellow, R.color.yellow_tint),
    Component2(R.color.green, R.color.green_tint),
    Component3(R.color.red, R.color.red_tint),
    Component4(R.color.blue, R.color.blue_tint),
    Component5(R.color.pink, R.color.pink_tint),
    ComponentTransparent(R.color.transparent, R.color.transparent)
    ;
}