package com.example.user.timecircle.common

object CommonUtil {
    fun Int.convertToCircleIndex(): Int = (this + CIRCLE_NUM) % CIRCLE_NUM
}