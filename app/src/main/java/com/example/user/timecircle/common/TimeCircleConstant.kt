package com.example.user.timecircle.common

import android.util.Log

const val DEBUG = true

fun cocoLog(msg: String, num: Int = 0) {
    Log.i("coco$num", msg)
}