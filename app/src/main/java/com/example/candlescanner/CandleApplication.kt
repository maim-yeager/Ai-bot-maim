package com.example.candlescanner

import android.app.Application
import android.util.Log

class CandleApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        try {
            System.loadLibrary("opencv_java4")
            Log.i("CandleApplication", "Loaded OpenCV native library")
        } catch (e: UnsatisfiedLinkError) {
            Log.w("CandleApplication", "Could not load OpenCV native library: ${e.message}")
        }
    }
}
