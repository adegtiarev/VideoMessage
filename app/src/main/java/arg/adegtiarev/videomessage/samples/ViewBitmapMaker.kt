package com.sololearn.app.ui.stories.recorder.creation

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.Rect
import android.os.Handler
import android.view.PixelCopy
import android.view.View
import androidx.core.view.drawToBitmap
import androidx.core.graphics.createBitmap

object ViewBitmapMaker {

    fun captureView(view: View): Bitmap {
        return view.drawToBitmap(Bitmap.Config.RGB_565)
    }

    fun captureViewAsync(activity: Activity, view: View, sendBitmap: (Bitmap) -> Unit) {
        val bitmap = createBitmap(view.width, view.height)
        val locationOfViewInWindow = IntArray(2)
        view.getLocationInWindow(locationOfViewInWindow)
        PixelCopy.request(
            activity.window,
            Rect(
                locationOfViewInWindow[0],
                locationOfViewInWindow[1],
                locationOfViewInWindow[0] + view.width,
                locationOfViewInWindow[1] + view.height
            ), bitmap, { copyResult ->
                if (copyResult == PixelCopy.SUCCESS) {
                    sendBitmap(bitmap)
                }
            }, Handler()
        )
    }
}