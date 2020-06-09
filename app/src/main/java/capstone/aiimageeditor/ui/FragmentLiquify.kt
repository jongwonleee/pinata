package capstone.aiimageeditor.ui

import android.R.attr.button
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.SeekBar
import androidx.fragment.app.Fragment
import capstone.aiimageeditor.ImageManager
import capstone.aiimageeditor.R
import capstone.aiimageeditor.imageprocessing.GPUImageFilterTools
import capstone.aiimageeditor.symmenticsegmentation.MaskSeparator
import com.google.android.material.tabs.TabLayout
import jp.co.cyberagent.android.gpuimage.GPUImage
import jp.co.cyberagent.android.gpuimage.GPUImageView
import jp.co.cyberagent.android.gpuimage.filter.GPUImageFilter


//import for liquify
import androidx.appcompat.app.AppCompatActivity
import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.os.Build
//import android.support.annotation.RequiresApi
import android.util.AttributeSet
import android.view.KeyEvent.ACTION_DOWN
import android.view.KeyEvent.ACTION_UP
import android.view.MotionEvent
import android.view.MotionEvent.ACTION_MOVE
//import android.view.View
import kotlin.math.pow
import android.util.Log

class LiquifyView :Fragment() {

    private lateinit var seekBar: SeekBar
    private lateinit var imageBG: ImageView
    private lateinit var imageFG: ImageView
    private lateinit var gpuImage: GPUImage
    //private lateinit var tabLayout: TabLayout
    private lateinit var imageManager: ImageManager
    private lateinit var maskSeparator: MaskSeparator
    //private var filters = arrayListOf<GPUImageFilter?>()
    //private var adjusts = arrayListOf<Int>()
    //private var tabPosition=0
    //private var filterAdjuster: GPUImageFilterTools.FilterAdjuster? = null



    //for liquify
    private var meshWidth = 30
    private var meshHeight = 60
    private var xstart = 0f
    private var ystart = 0f
    private lateinit var sorted: List<Pair<Float, Float>>
    private lateinit var original: List<Pair<Float, Float>>
    private lateinit var selectedIndex: MutableList<Int>
    private lateinit var coordinates: List<Pair<Float, Float>>
    private var paint = Paint()

    override fun onCreate(savedInstanceState: Bundle?) {
        selectedIndex = MutableList<Int>(1000,{_ -> 0})
        generateCoordinates()

        paint.color = Color.BLACK



        super.onCreate(savedInstanceState)
        seekBar = view.findViewById(R.id.seekBar)
        imageBG = view.findViewById(R.id.image_bg)
        imageFG = view.findViewById(R.id.image_fg)
        //tabLayout = view.findViewById(R.id.tabLayout)

        maskSeparator = MaskSeparator()
        imageManager = (activity?.application as ImageManager)

        gpuImage = GPUImage(context)

        imageBG.visibility=View.VISIBLE
        seekBar.max=100
        seekBar.progress=50
        seekBar.visibility=View.GONE
        setImage()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        seekBar = view.findViewById(R.id.seekBar)
        imageBG = view.findViewById(R.id.image_bg)
        imageFG = view.findViewById(R.id.image_fg)
        //tabLayout = view.findViewById(R.id.tabLayout)

        maskSeparator = MaskSeparator()
        imageManager = (activity?.application as ImageManager)

        gpuImage = GPUImage(context)

        imageBG.visibility=View.VISIBLE
        seekBar.max=100
        seekBar.progress=50
        seekBar.visibility=View.GONE

    }


    public fun setImage(){
        imageFG.setImageBitmap(imageManager.personOriginal)
        imageBG.setImageBitmap(imageManager.backgroundFiltered)
        gpuImage.setImage(imageManager.personOriginal)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_person, container, false)
    }











    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        generateCoordinates()
    }
    private fun generateCoordinates() {
        coordinates = generateCoordinate(
            meshWidth,
            meshHeight,
            width,
            height,
            paddingStart,
            paddingEnd,
            paddingTop,
            paddingBottom
        )
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        canvas.drawBitmapMesh(              //2차원좌표를 1차원배열로 표현
            bitmap,
            meshWidth,
            meshHeight,
            coordinates.flatMap { listOf(it.first, it.second) }.toFloatArray(),
            0,
            null,
            0,
            null
        )
        drawCoordinates(canvas)
        drawLines(canvas)
    }


    private fun drawCoordinates(canvas: Canvas) {

        coordinates.forEach {
            canvas.drawPoint(it.first, it.second, paint)
        }
    }
    private fun drawLines(canvas: Canvas) {
        coordinates.forEachIndexed { index, pair ->
            // Draw horizontal line with next column
            if (((index + 1) % (meshWidth + 1)) != 0) {
                val nextCoordinate = coordinates[index + 1]
                drawLine(canvas, pair, nextCoordinate)
            }

            // Draw horizontal line with next row
            if (((index < (meshWidth + 1) * meshHeight))) {
                val nextCoordinate = coordinates[index + meshWidth + 1]
                drawLine(canvas, pair, nextCoordinate)
            }
        }
    }
    private fun drawLine(
        canvas: Canvas,
        pair: Pair<Float, Float>,
        nextCoordinate: Pair<Float, Float>
    ) {
        canvas.drawLine(
            pair.first, pair.second,
            nextCoordinate.first, nextCoordinate.second,
            paint
        )
    }




    private fun isEdge(i: Int): Boolean {
        if (i >= 0 && i <= meshWidth) return true
        else if (i % (meshWidth + 1) == 0 || i % (meshWidth + 1) == meshWidth) return true
        else if (i >= (meshWidth + 1) * (meshHeight) && i <= (meshWidth + 1) * (meshHeight + 1) - 1) return true
        else return false
    }
    //@RequiresApi(Build.VERSION_CODES.O)
    private fun weight(i: Int): Float {
        var x: Int = i / 35 + 1
        //  var res: Float = Math.sqrt(Math.sqrt(Math.sqrt(Math.sqrt(Math.sqrt(x.toDouble()))))).toFloat()

        var res: Float = x.toFloat().pow(0.031f)
        Log.i("##", "" + res + " " )
        return 1 / res
    }

    //private var selectedIndex = mutableListOf<Int>()
    //@RequiresApi(Build.VERSION_CODES.O)
    override fun onTouchEvent(event: MotionEvent): Boolean {
        var total = coordinates.size - 1 //총 점들 갯수 -1
        //selectedIndex = MutableList<Int>(1000, { _ -> 0 })

        when (event.action) {
            ACTION_DOWN -> {
                xstart = event.x
                ystart = event.y
                original = coordinates
                sorted =
                    coordinates.sortedBy { (it.first - xstart).pow(2) + (it.second - ystart).pow(2) }


//                Log.i("@@" , "----------------------")
//                for (i in 0..total) {
//                    Log.i("@@@@@", "" + coordinates.indexOf(sorted[i]))
//                }
//                for (i in 0..total) {
//                    Log.i("@@" , "" + coordinates[i].first + " " + coordinates[i].second)
//                }
//                for (i in 0..total) {
//                    Log.i("@@@", "" + sorted[i].first + " " + sorted[i].first)
//                }

                for (i in 0..total) {
                    val idx = coordinates.indexOf(sorted[i])
                    if (isEdge(idx)) selectedIndex[i] = -1 else selectedIndex[i] = idx
                    //selectedIndex[i] = idx
                }
                return true
            }


            ACTION_MOVE, ACTION_UP -> { //떼, 움직여
                var xmove = (event.x - xstart) / 20
                var ymove = (event.y - ystart) / 20
                //Log.i("!!", "xmove " + xmove + "ymove " + ymove)
                //Log.i("!! ", "index " + selectedIndex[0] + " " + selectedIndex[1]+ " " + selectedIndex[2] + " " + selectedIndex[3])

                for (i in 0..total) {
                    xmove *= weight(i)
                    ymove *= weight(i)
                    var s = selectedIndex[i]

                    coordinates =
                        coordinates.mapIndexed { index, pair -> if (index == s) ((original[s].first + xmove) to (original[s].second + ymove)) else pair }
                    //Log.i("!!", "x " + coordinates[ selectedIndex[i] ].first + " y " +  coordinates[ selectedIndex[i] ].second + "\n")
                }
                return true
            }
        }


        return false
    }

    private fun generateCoordinate(
        col: Int, row: Int, width: Int, height: Int,
        paddingStart: Int = 0, paddingEnd: Int = 0,
        paddingTop: Int = 0, paddingBottom: Int = 0
    ): List<Pair<Float, Float>> {

        val widthSlice = (width - (paddingStart + paddingEnd)) / (col)
        val heightSlice = (height - (paddingTop + paddingBottom)) / (row)

        val coordinates = mutableListOf<Pair<Float, Float>>()

        for (y in 0..row) {
            for (x in 0..col) {
                coordinates.add(
                    Pair(
                        (x * widthSlice + paddingStart).toFloat(),
                        (y * heightSlice + paddingTop).toFloat()
                    )
                )
            }
        }

        return coordinates
    }
}



















