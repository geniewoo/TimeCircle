package com.example.user.timecircle

sealed class TouchMode {
    object None : TouchMode()
    class Rotating(val index: Int) : TouchMode()
    class AdjustActivity(val activitySetIndex: Int = 0, val adjustDirection: AdjustDirection) : TouchMode() {
        constructor(unconfirmed: Unconfirmed) : this(unconfirmed.activitySetIndex, unconfirmed.adjustDirection!!)
    }

    class MoveActivity(val touchedIndex: Int, val activitySet: ActivitySet) : TouchMode()
    class Unconfirmed(val activitySetIndex: Int = 0, val activitySet: ActivitySet, val adjustDirection: AdjustDirection?) : TouchMode()
}

enum class AdjustDirection {
    CLOCKWISE,
    ANTICLOCKWISE,
    BOTH;
}
