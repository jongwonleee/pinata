package capstone.aiimageeditor

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.SeekBar
import androidx.fragment.app.Fragment
import com.google.android.material.tabs.TabLayout


class FragmentMask : Fragment() {

    private lateinit var seekBar: SeekBar
    private lateinit var imageBG:ImageView
    private lateinit var imageFG: LinearLayout
    private lateinit var tabLayout:TabLayout
    public lateinit var imageManager:ImageManager
    private lateinit var mask:MyView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        seekBar = view.findViewById(R.id.seekBar)
        imageBG = view.findViewById(R.id.image_bg)
        imageFG = view.findViewById(R.id.image_fg)
        tabLayout = view.findViewById(R.id.tabLayout)
        imageManager = (activity?.application as ImageManager)
    }

    fun setImage(){
        mask = MyView(context!!,imageManager.mask)
        imageBG.setImageBitmap (imageManager.original)
        //imageFG.setImageBitmap(imageManager.mask)
        imageFG.addView(mask)
        val bitmap = Bitmap.createBitmap(imageManager.mask.width,imageManager.mask.height, Bitmap.Config.ARGB_8888)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_mask, container, false)
    }
    internal class Point(
        var x: Float,
        var y: Float,
        var check: Boolean,
        var color: Int
    )

    internal class MyView(context: Context,mask:Bitmap) : View(context) {

        var points = arrayListOf<Point>()
        var color: Int = Color.BLACK
        override fun onDraw(canvas: Canvas) {
           // canvas.drawBitmap(mask,0,0,)
            val p = Paint()
            p.strokeWidth = 15f
            for (i in 1 until points.size) {
                p.color = points[i].color
                if (!points[i].check) continue
                canvas.drawLine(points[i - 1].x, points[i - 1].y, points[i].x, points[i].y, p)
            }
        }

        override fun onTouchEvent(event: MotionEvent): Boolean {
            val x: Float = event.x
            val y: Float = event.y
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    points.add(Point(x, y, false, color))
                    points.add(Point(x, y, true, color))
                }
                MotionEvent.ACTION_MOVE -> points.add(Point(x, y, true, color))
                MotionEvent.ACTION_UP -> {
                }
            }
            invalidate()
            return true
        }
    }


}
