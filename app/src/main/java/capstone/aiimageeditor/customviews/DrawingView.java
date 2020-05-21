package capstone.aiimageeditor.customviews;


import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.util.Log;
import android.view.View;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.MotionEvent;

import androidx.annotation.Nullable;

public class DrawingView extends View {
    private Path drawPath;
    private Paint drawPaint, canvasPaint,pointPaint;
    private int paintColor = Color.RED;
    private Canvas drawCanvas;
    private Bitmap canvasBitmap;
    private Bitmap mask;
    private int minx,miny,maxx,maxy;
    private float touchX, touchY;
    private boolean isTouched;
    public DrawingView(Context context,Bitmap mask){

        super(context);
        this.mask = mask;
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
        int width=mask.getWidth();
        int height=mask.getHeight();
        Log.i("!!",width+","+height+" "+w+","+h);
        if(width/height > w/h){
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
        Log.i("!!",width+","+height+" "+w+","+h);
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
        touchX = event.getX();
        touchY = event.getY();
        float brushSize = drawPaint.getStrokeWidth()/2;
        if(touchX>maxx-brushSize|| touchX<minx+brushSize) return true;
        if(touchY>maxy-brushSize || touchY<miny+brushSize) return true;
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
                return false;
        }

        invalidate();
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
        Bitmap ret = Bitmap.createBitmap(mask.getWidth(),mask.getHeight(), Bitmap.Config.ARGB_8888);
        for(int x=0;x<mask.getWidth();x++){
            for(int y=0;y<mask.getHeight();y++){
                if(canvasBitmap.getPixel(x=minx,y+miny)!=Color.RED) ret.setPixel(x,y,Color.BLACK);
                else ret.setPixel(x,y,Color.WHITE);
            }
        }
        return ret;
    }

}