package com.example.user.timecircle

class ActivitySetManager {
    private val activitySetList = arrayListOf<ActivitySet>()

    fun makeActivitySet(index: Int): ActivitySet {
        val newActivityFromIndex = if (isActivitySetExist(index - 1)) index else index - 1
        val newActivityToIndex = if (isActivitySetExist(index + 1)) index else index + 1
        return ActivitySet(newActivityFromIndex, newActivityToIndex)
    }

    fun isActivitySetExist(index: Int): Boolean {
        return activitySetList.any {
            if (index < it.fromIndex) return@any false
            it.fromIndex <= index && index <= it.toIndex
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
}

class ActivitySet(var fromIndex: Int, var toIndex: Int)
