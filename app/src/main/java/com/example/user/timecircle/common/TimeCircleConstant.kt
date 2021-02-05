package com.example.user.timecircle.common

import android.util.Log

const val DEBUG = true
const val CIRCLE_NUM = 144
const val UNIT_ANGLE = 360.0f / CIRCLE_NUM

fun cocoLog(msg: String, num: Int = 0) {
    Log.i("coco$num", msg)
}