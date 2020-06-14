package capstone.aiimageeditor.customviews;


import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.util.Log;
import android.view.GestureDetector;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.view.MotionEvent;

import org.jetbrains.annotations.NotNull;

import capstone.aiimageeditor.ZoomGestureListener;

public class DrawingView extends View implements ZoomGestureListener.OnTouchListener {
    private Path drawPath;
    private Paint drawPaint, canvasPaint,pointPaint;
    private int paintColor = Color.RED;
    private Canvas drawCanvas;
    private Bitmap canvasBitmap;
    private Bitmap mask;
    private int minx,miny,maxx,maxy,width,height;
    private float touchX, touchY;
    private boolean isTouched;
    private ZoomGestureListener zoomGestureListener;
    private ScaleGestureDetector scaleGestureDetector;
    private GestureDetector gestureDetector;
    public DrawingView(Context context,  Bitmap mask){

        super(context);
        this.mask = mask;
        zoomGestureListener = new ZoomGestureListener();
        zoomGestureListener.setOnTouchListener(this);
        gestureDetector = zoomGestureListener.getZoomGestureDetector(context);
        scaleGestureDetector = zoomGestureListener.getScaleGestureDetector(context);

        setupDrawing();
    }

    private void setupDrawing(){
        isTouched=false;
        drawPath = new Path();
        drawPaint = new Paint();
        pointPaint = new Paint();
        pointPaint.setColor(Color.LTGRAY);
        pointPaint.setStyle(Paint.Style.STROKE);
        pointPaint.setAntiAlias(true);
        drawPaint.setStrokeWidth(3);
        drawPaint.setColor(paintColor);
        drawPaint.setAntiAlias(true);
        drawPaint.setStrokeWidth(50);
        drawPaint.setStyle(Paint.Style.STROKE);
        drawPaint.setStrokeJoin(Paint.Join.ROUND);
        drawPaint.setStrokeCap(Paint.Cap.ROUND);
        canvasPaint = new Paint(Paint.DITHER_FLAG);
    }

    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged( w, h, oldw, oldh);
        width=mask.getWidth();
        height=mask.getHeight();

        if((float)width/(float)height > (float)w/(float)h){
            height = height * w/width;
            width=w;
        }else
        {
            width = width * h/height;
            height= h;
        }
        minx=(w-width)/2;
        miny=(h-height)/2;
        maxx=minx+width;
        maxy=miny+height;
        //Bitmap scaledBitmap = Bitmap.createScaledBitmap(mask,width,height,true);
        Bitmap scaledBitmap = Bitmap.createScaledBitmap(getDrawableBitmap(),width,height,true);
        canvasBitmap = Bitmap.createBitmap( w, h, Bitmap.Config.ARGB_8888);
        drawCanvas = new Canvas(canvasBitmap);
        drawCanvas.drawBitmap(scaledBitmap,minx,miny,null);

    }

    public void setStrokeWidth(float val){
        drawPaint.setStrokeWidth(val);
    }

    public void setBrush(boolean brush){
        if(brush){
            drawPaint.setColor(Color.RED);
            drawPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.ADD));
        }
        else {
            drawPaint.setColor(Color.BLACK);
            drawPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        }
    }

    protected void onDraw(Canvas canvas) {
        canvas.drawBitmap(canvasBitmap, 0, 0, canvasPaint);
        canvas.drawPath(drawPath, drawPaint);
        if(isTouched)canvas.drawCircle(touchX,touchY,drawPaint.getStrokeWidth()/2,pointPaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
       /* gestureDetector.onTouchEvent(event);
        scaleGestureDetector.onTouchEvent(event);*/

        if(!zoomGestureListener.getOnScaling()){
            touchX = event.getX();
            touchY = event.getY();
            float brushSize = drawPaint.getStrokeWidth()/2;
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    drawPath.moveTo(touchX, touchY);
                    isTouched=true;
                    break;
                case MotionEvent.ACTION_MOVE:
                    drawPath.lineTo(touchX, touchY);
                    break;
                case MotionEvent.ACTION_UP:
                    drawPath.lineTo(touchX, touchY);
                    drawCanvas.drawPath(drawPath, drawPaint);
                    drawPath.reset();
                    isTouched=false;
                    break;
                default:
            }

            invalidate();
        }

        return true;
    }

    private Bitmap getDrawableBitmap(){
        Bitmap ret = Bitmap.createBitmap(mask.getWidth(),mask.getHeight(), Bitmap.Config.ARGB_8888);
        for(int x=0;x<mask.getWidth();x++){
            for(int y=0;y<mask.getHeight();y++){
                if(mask.getPixel(x,y)!=Color.WHITE) ret.setPixel(x,y,Color.TRANSPARENT);
                else ret.setPixel(x,y,Color.RED);
            }
        }
        return ret;
    }

    public Bitmap getMask(){
        Bitmap ret = Bitmap.createBitmap(canvasBitmap,minx,miny,width,height);
        for(int x=0;x<ret.getWidth();x++){
            for(int y=0;y<ret.getHeight();y++){
                if(ret.getPixel(x,y)==0) ret.setPixel(x,y,Color.TRANSPARENT);
                else ret.setPixel(x,y,Color.WHITE);
            }
        }
        ret = Bitmap.createScaledBitmap(ret,mask.getWidth(),mask.getHeight(),true);
        return ret;
    }

    @Override
    public void onScale(float scale, int x, int y) {
        Log.i("!!",scale +" "+ x+ " "+y);
        int w = canvasBitmap.getWidth();
        int h = canvasBitmap.getHeight();
        int width= (int)(this.width*scale);
        int height= (int)(this.height*scale);

        minx=(w-width)/2;
        miny=(h-height)/2;
        maxx=minx+width;
        maxy=miny+height;
        Bitmap scaledBitmap = Bitmap.createScaledBitmap(canvasBitmap,width,height,true);
        canvasBitmap = Bitmap.createBitmap( w, h, Bitmap.Config.ARGB_8888);
        drawCanvas = new Canvas(canvasBitmap);
        drawCanvas.drawBitmap(scaledBitmap,minx,miny,null);
        invalidate();
    }

    @Override
    public void onDraw(@NotNull MotionEvent motionEvent) {

    }
}