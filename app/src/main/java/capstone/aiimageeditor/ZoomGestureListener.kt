package capstone.aiimageeditor

import android.content.Context
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import java.util.logging.Handler

class ZoomGestureListener : GestureDetector.SimpleOnGestureListener() {
    val MAXSCALE = 1.5f
    val MINSCALE = 0.5f
    var scale = 1.0f
    var onTouchListener: OnTouchListener? = null
    var onScaling = false
    inline fun getScaleGestureDetector(context: Context) = ScaleGestureDetector(context, ScaleGestureListener())

    inline fun getZoomGestureDetector(context: Context) = GestureDetector(context, this)


    interface OnTouchListener {
        fun onDraw(motionEvent: MotionEvent)
        fun onScale(scale: Float, x: Int, y: Int)
    }

    override fun onDoubleTap(e: MotionEvent?): Boolean {
        if (scale == MAXSCALE) scale = MINSCALE
        else scale = MAXSCALE
        onTouchListener?.onScale(scale, e!!.x.toInt(), e.y.toInt())
        return super.onDoubleTap(e)
    }

    override fun onShowPress(e: MotionEvent?) {
        super.onShowPress(e)
    }

    override fun onScroll(
        e1: MotionEvent?,
        e2: MotionEvent?,
        distanceX: Float,
        distanceY: Float
    ): Boolean {
        onTouchListener?.onDraw(e1!!)
        return super.onScroll(e1, e2, distanceX, distanceY)
    }

    inner class ScaleGestureListener : ScaleGestureDetector.SimpleOnScaleGestureListener() {
        override fun onScaleBegin(detector: ScaleGestureDetector?): Boolean {
            onScaling = true
            return super.onScaleBegin(detector)
        }

        override fun onScaleEnd(detector: ScaleGestureDetector?) {
            val scaleFactor = detector!!.scaleFactor
            // 최대 10배, 최소 10배 줌 한계 설정
            scale = if (scaleFactor > 1.0f) 2f else 1f
            onTouchListener?.onScale(scale, detector.focusX.toInt(), detector.focusY.toInt())
            android.os.Handler().postDelayed({
                onScaling = false
            }, 200)
            super.onScaleEnd(detector)
        }


    }
}

