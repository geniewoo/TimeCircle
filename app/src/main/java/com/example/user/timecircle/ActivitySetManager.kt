package com.example.user.timecircle

import com.example.user.timecircle.common.CIRCLE_NUM

class ActivitySetManager {
    private val activitySetList = arrayListOf<ActivitySet>()
    private var lastAdjustingIndex: Int? = null

    fun makeActivitySet(index: Int, component: ActivityComponent): ActivitySet {
        val newActivityFromIndex = if (findActivitySet(index - 1) != null) index else index - 1
        val newActivityToIndex = if (findActivitySet(index + 1) != null) index else index + 1
        return ActivitySet(newActivityFromIndex, newActivityToIndex, component)
    }

    fun findActivitySet(index: Int): ActivitySet? {
        return activitySetList.find {
            if (index < it.fromIndex) return@find false
            it.fromIndex <= index && index <= it.toIndex
        }
    }

    fun returnActivityUnconfirmedMode(index: Int): TouchMode? {
        val setIndex = activitySetList.indexOfFirst {
            if (index < it.fromIndex) return@indexOfFirst false
            it.fromIndex == index || index == it.toIndex
        }
        return findActivitySet(index)?.let {
            val adjustDirection = if (it.fromIndex == index || index == it.toIndex) {
                when {
                    activitySetList[setIndex].fromIndex != index -> AdjustDirection.CLOCKWISE
                    activitySetList[setIndex].toIndex != index -> AdjustDirection.ANTICLOCKWISE
                    else -> AdjustDirection.BOTH
                }
            } else null
            TouchMode.Unconfirmed(setIndex, it, adjustDirection)
        }
    }

    fun insertActivitySet(activitySet: ActivitySet) {
        val insertIndex = activitySetList.indexOfFirst {
            activitySet.fromIndex < it.fromIndex
        }
        if (insertIndex >= 0)
            activitySetList.add(insertIndex, activitySet)
        else
            activitySetList.add(activitySet)
    }

    fun adjustAvailableIndex(touchedIndex: Int, adjustActivity: TouchMode.AdjustActivity, result: (fromIndex: Int, toIndex: Int, component: ActivityComponent) -> Unit) {
        val activitySet = activitySetList[adjustActivity.activitySetIndex]
        val prevToIndex = activitySetList.elementAtOrNull(adjustActivity.activitySetIndex - 1)?.toIndex
                ?: 0
        val nextFromIndex = activitySetList.elementAtOrNull(adjustActivity.activitySetIndex + 1)?.fromIndex
                ?: CIRCLE_NUM - 1

        when (adjustActivity.adjustDirection) {
            AdjustDirection.CLOCKWISE -> {
                val validTouchedIndex = touchedIndex.coerceIn(activitySet.fromIndex, nextFromIndex - 1)
                adjustOnClockwise(validTouchedIndex, activitySet, result)
                lastAdjustingIndex = validTouchedIndex
            }
            AdjustDirection.ANTICLOCKWISE -> {
                val validTouchedIndex = touchedIndex.coerceIn(prevToIndex + 1, activitySet.toIndex)
                adjustOnAntiClockwise(validTouchedIndex, activitySet, result)
                lastAdjustingIndex = validTouchedIndex
            }
            AdjustDirection.BOTH -> {
                val validTouchedIndex = touchedIndex.coerceIn(prevToIndex + 1, nextFromIndex - 1)
                when {
                    validTouchedIndex <= activitySet.fromIndex && (lastAdjustingIndex
                            ?: -1) <= activitySet.fromIndex -> {
                        adjustOnAntiClockwise(validTouchedIndex, activitySet, result)
                    }
                    validTouchedIndex >= activitySet.fromIndex && (lastAdjustingIndex
                            ?: CIRCLE_NUM) >= activitySet.fromIndex -> {
                        adjustOnClockwise(validTouchedIndex, activitySet, result)
                    }
                    validTouchedIndex < activitySet.fromIndex && (lastAdjustingIndex
                            ?: -1) > activitySet.fromIndex -> {
                        result(validTouchedIndex, activitySet.fromIndex, activitySet.component)
                        result(activitySet.fromIndex + 1, lastAdjustingIndex
                                ?: return, ActivityComponent.ComponentTransparent)
                    }
                    validTouchedIndex > activitySet.fromIndex && (lastAdjustingIndex
                            ?: CIRCLE_NUM) < activitySet.fromIndex -> {
                        result(activitySet.fromIndex, validTouchedIndex, activitySet.component)
                        result(lastAdjustingIndex
                                ?: return, activitySet.fromIndex - 1, ActivityComponent.ComponentTransparent)
                    }
                    else -> return
                }
                lastAdjustingIndex = validTouchedIndex
            }
        }
    }

    private fun adjustOnClockwise(touchedIndex: Int, activitySet: ActivitySet, result: (fromIndex: Int, toIndex: Int, component: ActivityComponent) -> Unit) {
        (lastAdjustingIndex ?: activitySet.toIndex).let {
            if (touchedIndex > it) {
                result(it + 1, touchedIndex, activitySet.component)
            } else if (touchedIndex < it) {
                result(touchedIndex + 1, it, ActivityComponent.ComponentTransparent)
            }
        }
    }

    private fun adjustOnAntiClockwise(touchedIndex: Int, activitySet: ActivitySet, result: (fromIndex: Int, toIndex: Int, component: ActivityComponent) -> Unit) {
        (lastAdjustingIndex ?: activitySet.fromIndex).let {
            if (touchedIndex < it) {
                result(touchedIndex, it - 1, activitySet.component)
            } else if (touchedIndex > it) {
                result(it, touchedIndex - 1, ActivityComponent.ComponentTransparent)
            }
        }
    }

    fun adjustActivityDone(adjustActivity: TouchMode.AdjustActivity) {
        val activitySet = activitySetList[adjustActivity.activitySetIndex]
        lastAdjustingIndex?.let { lastIndex ->
            when (adjustActivity.adjustDirection) {
                AdjustDirection.CLOCKWISE -> activitySet.toIndex = lastIndex
                AdjustDirection.ANTICLOCKWISE -> activitySet.fromIndex = lastIndex
                AdjustDirection.BOTH -> if (activitySet.fromIndex > lastIndex) {
                    activitySet.fromIndex = lastIndex
                } else {
                    activitySet.toIndex = lastIndex
                }
            }
        }

        lastAdjustingIndex = null
    }

    fun removeActivity(activitySet: ActivitySet) {
        activitySetList.remove(activitySet)
    }
}

class ActivitySet(var fromIndex: Int, var toIndex: Int, val component: ActivityComponent)
