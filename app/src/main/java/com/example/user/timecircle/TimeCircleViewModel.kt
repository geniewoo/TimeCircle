package com.example.user.timecircle

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class TimeCircleViewModel: ViewModel() {
    val isZoom = MutableLiveData<Boolean>().apply { value = false }
}