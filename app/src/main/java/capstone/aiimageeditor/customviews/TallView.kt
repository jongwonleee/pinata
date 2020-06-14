package capstone.aiimageeditor.customviews

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import android.view.KeyEvent.ACTION_DOWN
import android.view.KeyEvent.ACTION_UP
import android.view.MotionEvent
import android.view.MotionEvent.ACTION_MOVE
import android.view.View
import kotlin.math.abs
import kotlin.math.pow


class TallView  @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
) : View(context, attrs, defStyleAttr, defStyleRes) {

    private var meshWidth = 45
    private var meshHeight = 30
    private var xstart = 0f
    private var ystart = 0f

    private var minx=0
    private var miny=0

    private lateinit var selectedIndex: MutableList<Int>
    private var paint = Paint()
    private lateinit var coordinates: List<Pair<Float, Float>>
    private lateinit var bitmap: Bitmap
    private lateinit var bgimg: Bitmap

    private var _width:Int = 0
    private var _height:Int = 0
    private lateinit var drawCanvas: Canvas
    private lateinit var canvasBitmap: Bitmap

    private var Lines = arrayOf(0f, 0f, 0f) //각각 목, 골반, 발
    private var a = 0 //목 인덱스
    private var b = 0 //골반 인덱스
    private var c = 0 //발 인덱스
    private var adjust = 0f //범위 : 1.00 ~ 1.30
    private var orig = 0f
    private var leg = 0f
    private var bodyBlocksize = 0f
    private var legBlocksize = 0f


    fun setup(column: Int, row: Int, img: Bitmap, backgroundimg:Bitmap) {
        meshWidth = column
        meshHeight = row
        bitmap = img
        bgimg = backgroundimg
        _width = bgimg.width
        _height = bgimg.height
        selectedIndex = MutableList<Int>(300,{_ -> 0})
        paint.color = Color.BLACK

//        coordinates = generateCoordinate(
//        meshWidth,
//        meshHeight,
//        width,
//        height,
//        paddingStart,
//        paddingEnd,
//        paddingTop,
//        paddingBottom
//        )
        generateCoordinates()
        invalidate() //ondraw호출
    }



    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)

        if (_width.toFloat() / _height.toFloat() > w.toFloat() / h.toFloat()) {
            _height = _height * w / _width
            _width = w
        } else {
            _width = _width * h / _height
            _height = h
        }
        minx = (w - _width) / 2
        miny = (h - _height) / 2
        canvasBitmap = Bitmap.createBitmap(_width, _height, Bitmap.Config.ARGB_8888)
        drawCanvas = Canvas(canvasBitmap)
        bgimg = Bitmap.createScaledBitmap(bgimg,_width,_height,true)
        generateCoordinates()

        initialize(207f, 320f, 481f) //initialize after generateCoordinates
    }



    private fun generateCoordinates() {
        coordinates = generateCoordinate(
            meshWidth,
            meshHeight,
            _width,
            _height,
            paddingStart,
            paddingEnd,
            paddingTop,
            paddingBottom
        )
    }


    fun removeLines() {
        val drawPaint = Paint()
        drawPaint.setColor(Color.BLACK)
        drawPaint.setXfermode(PorterDuffXfermode(PorterDuff.Mode.CLEAR))
        drawCanvas.drawRect(0f,0f, drawCanvas.width.toFloat(),drawCanvas.height.toFloat(),drawPaint)

        //canvas.drawBitmap(bgimg,minx.toFloat(),miny.toFloat(), Paint())
        paint.color=Color.argb(128,255,255,255)
        //canvas.drawRect(0f,0f,width.toFloat(),height.toFloat(),paint)

        drawCanvas.drawBitmapMesh(
            bitmap,
            meshWidth,
            meshHeight,     //2차원좌표를 1차원배열로 표현
            coordinates.flatMap { listOf(it.first, it.second) }.toFloatArray(),
            0,
            null,
            0,
            null
        )

        //drawCoordinates(drawCanvas)
        //drawLines(drawCanvas)

        //canvas.drawBitmap(canvasBitmap,minx.toFloat(),miny.toFloat(), Paint())
    }
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val drawPaint = Paint()
        drawPaint.setColor(Color.BLACK)
        drawPaint.setXfermode(PorterDuffXfermode(PorterDuff.Mode.CLEAR))

        val LinePaint = Paint()
        LinePaint.setStrokeWidth(6f)
        LinePaint.setStyle(Paint.Style.FILL)
        LinePaint.setColor(Color.RED)

        drawCanvas.drawRect(0f,0f, drawCanvas.width.toFloat(),drawCanvas.height.toFloat(),drawPaint)



        //canvas.drawBitmap(bgimg,minx.toFloat(),miny.toFloat(), Paint())
        paint.color=Color.argb(128,255,255,255)
        canvas.drawRect(0f,0f,width.toFloat(),height.toFloat(),paint)

        drawCanvas.drawBitmapMesh(
            bitmap,
            meshWidth,
            meshHeight,     //2차원좌표를 1차원배열로 표현
            coordinates.flatMap { listOf(it.first, it.second) }.toFloatArray(),
            0,
            null,
            0,
            null
        )

        //drawCoordinates(drawCanvas)
        //drawLines(drawCanvas)

        canvas.drawBitmap(canvasBitmap,minx.toFloat(),miny.toFloat(), Paint())

        //drawcanvas or canvas 어디에 그리나?
        drawCanvas.drawLine(0f, Lines[0], drawCanvas.width.toFloat(), Lines[0], LinePaint)
        drawCanvas.drawLine(0f, Lines[1], drawCanvas.width.toFloat(), Lines[1], LinePaint)
        drawCanvas.drawLine(0f, Lines[2], drawCanvas.width.toFloat(), Lines[2], LinePaint)
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





    fun initialize(A:Float, B:Float, C:Float) {
        //선 3개 그려
        Lines[0] = A
        Lines[1] = B
        Lines[2] = C
        invalidate()

        a = nearestCoordinate(A.toInt()) //가장 가까운 정점의 인덱스로 업데이트
        b = nearestCoordinate(B.toInt())
        c = nearestCoordinate(C.toInt())
        orig = coordinates[2].second - coordinates[0].second //원래 한칸의 길이
    }

    fun tall(adj : Float) {
        adjust = adj
        leg = 1.00f + (adjust - 1.00f)*1.5f
        bodyBlocksize = adjust * orig
        legBlocksize = leg * adjust * orig

        var afirst = coordinates[a].second

        //다리부분
        var idx = 0
        for (i in c-1 downTo b) { //c ~ b
            coordinates = coordinates.mapIndexed { index, pair -> if (index == i) (coordinates[i].first to (coordinates[c].second - (idx/2 + 1)*legBlocksize)) else pair }
            idx++
        }

        //몸통부분
        idx = 0
        for (i in b-1 downTo a) { //b ~ a
            coordinates = coordinates.mapIndexed { index, pair -> if (index == i) (coordinates[i].first to (coordinates[b].second - (idx/2 + 1)*bodyBlocksize)) else pair }
            idx++
        }

        //몸통 윗부분
        var d = afirst - coordinates[a].second
        for (i in a-1 downTo 0) //a-1 ~ 0
            coordinates = coordinates.mapIndexed { index, pair -> if (index == i) (coordinates[i].first to coordinates[i].second  - d) else pair }


        Lines[0] = coordinates[a].second
        Lines[1] = coordinates[b].second
        Lines[2] = coordinates[c].second
        invalidate()
    }
    private fun nearestCoordinate (i : Int) : Int {
        val sorted = coordinates.sortedBy { abs(it.second - i) }
        return coordinates.indexOf(sorted[0])
    }





    //Lines중 가장 가까운 선분 인덱스 리턴
    private fun nearestLine (y : Float) :Int{
        var min = abs(Lines[0] - y )
        var idx = 0

        if (min > abs(Lines[1] - y)) {
            min = abs(Lines[1] - y)
            idx = 1
        }
        if (min > abs(Lines[2] - y)) {
            min = abs(Lines[2] - y)
            idx = 2
        }

        return idx
    }

    var selectedLineIdx = 0
    override fun onTouchEvent(event: MotionEvent): Boolean {
        val gap = height/2 - _height/2
        when (event.action) {
            ACTION_DOWN -> {
                //a,b,c중 가장 가까운 선분 선택
                selectedLineIdx = nearestLine(event.y - gap)
                return true
            }
            ACTION_MOVE, ACTION_UP -> {
                Lines[selectedLineIdx] = event.y - gap
                Lines.sort() //line들 정렬
                initialize(Lines[0], Lines[1], Lines[2]) //크기순으로 넣기
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

        val widthSlice = (width.toFloat() - (paddingStart + paddingEnd)) / (col.toFloat())
        val heightSlice = (height.toFloat() - (paddingTop + paddingBottom)) / (row.toFloat())
        val coordinates = mutableListOf<Pair<Float, Float>>()

        for (y in 0..row) {
            for (x in 0..col) {
                coordinates.add(
                    Pair(
                        x * widthSlice + paddingStart,
                        y * heightSlice + paddingTop
                    )
                )
            }
        }

        return coordinates
    }

    public fun getTalledImage(width: Int,height: Int):Bitmap{
        val drawPaint = Paint()
        drawPaint.setColor(Color.BLACK)
        drawPaint.setXfermode(PorterDuffXfermode(PorterDuff.Mode.CLEAR))

        val LinePaint = Paint()
        LinePaint.setStrokeWidth(6f)
        LinePaint.setStyle(Paint.Style.FILL)
        LinePaint.setColor(Color.RED)

        drawCanvas.drawRect(0f,0f, drawCanvas.width.toFloat(),drawCanvas.height.toFloat(),drawPaint)





        drawCanvas.drawBitmapMesh(
            bitmap,
            meshWidth,
            meshHeight,     //2차원좌표를 1차원배열로 표현
            coordinates.flatMap { listOf(it.first, it.second) }.toFloatArray(),
            0,
            null,
            0,
            null
        )

        return Bitmap.createScaledBitmap(canvasBitmap,width,height,true)
    }

}
