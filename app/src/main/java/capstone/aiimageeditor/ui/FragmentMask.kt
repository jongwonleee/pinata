package capstone.aiimageeditor.ui

import android.content.Context
import android.graphics.*
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.SeekBar
import androidx.fragment.app.Fragment
import capstone.aiimageeditor.customviews.DrawingView
import capstone.aiimageeditor.ImageManager
import capstone.aiimageeditor.R
import com.google.android.material.tabs.TabLayout


class FragmentMask : Fragment() {

    private lateinit var seekBar: SeekBar
    private lateinit var imageBG:ImageView
    private lateinit var imageFG: LinearLayout
    private lateinit var tabLayout:TabLayout
    public lateinit var imageManager: ImageManager
    private lateinit var maskView: DrawingView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        seekBar = view.findViewById(R.id.seekBar)
        imageBG = view.findViewById(R.id.image_bg)
        imageFG = view.findViewById(R.id.image_fg)
        tabLayout = view.findViewById(R.id.tabLayout)
        imageManager = (activity?.application as ImageManager)

        seekBar.max=90
        seekBar.progress=45

        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                maskView.setStrokeWidth((p1+10).toFloat())
            }

            override fun onStartTrackingTouch(p0: SeekBar?) {
            }

            override fun onStopTrackingTouch(p0: SeekBar?) {
            }

        })
        tabLayout.addOnTabSelectedListener(object:TabLayout.OnTabSelectedListener{
            override fun onTabReselected(tab: TabLayout.Tab?) {
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
            }

            override fun onTabSelected(tab: TabLayout.Tab?) {
                when(tab?.position){
                    0->maskView.setBrush(true)
                    else->maskView.setBrush(false)
                }
            }

        })

    }

    fun setImage(){
        maskView = DrawingView(
            context!!,
            imageManager.mask
        )//,imageManager.mask)
        maskView.setStrokeWidth(55f)
        imageBG.setImageBitmap (imageManager.original)
        //imageFG.setImageBitmap(imageManager.mask)
        imageFG.addView(maskView)
        val bitmap = Bitmap.createBitmap(imageManager.mask.width,imageManager.mask.height, Bitmap.Config.ARGB_8888)

    }

    fun deleteView(){
        imageFG.removeView(maskView)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
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
                    points.add(
                        Point(
                            x,
                            y,
                            false,
                            color
                        )
                    )
                    points.add(
                        Point(
                            x,
                            y,
                            true,
                            color
                        )
                    )
                }
                MotionEvent.ACTION_MOVE -> points.add(
                    Point(
                        x,
                        y,
                        true,
                        color
                    )
                )
                MotionEvent.ACTION_UP -> {
                }
            }
            invalidate()
            return true
        }
    }

}
