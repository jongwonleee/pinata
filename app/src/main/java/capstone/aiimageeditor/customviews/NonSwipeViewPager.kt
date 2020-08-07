package capstone.aiimageeditor.customviews

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.viewpager.widget.ViewPager
import com.google.android.material.appbar.AppBarLayout


class NonSwipeViewPager(context: Context, attrs: AttributeSet) : ViewPager(context, attrs) {
    private var swipeEnabled = false
    private var setUnexpend = false
    private lateinit var appBarLayout: AppBarLayout
    override fun onTouchEvent(event: MotionEvent): Boolean {
        return when (swipeEnabled) {
            true -> super.onTouchEvent(event)
            false -> false
        }
    }

    override fun onInterceptTouchEvent(event: MotionEvent): Boolean {
        if (setUnexpend) {
            appBarLayout.setExpanded(false)
        }
        return when (swipeEnabled) {
            true -> super.onInterceptTouchEvent(event)
            false -> false
        }
    }

//    fun setSwipePagingEnabled(swipeEnabled: Boolean) {
//        this.swipeEnabled = swipeEnabled
//    }
//    fun setAutoUnexpendingAppbar(appBarLayout: AppBarLayout){
//        setUnexpend=true
//        this.appBarLayout = appBarLayout
//    }
}
