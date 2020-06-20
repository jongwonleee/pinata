package capstone.aiimageeditor.customviews;


import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.os.Build;
import android.util.Log;
import android.view.View;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.view.MotionEvent;

import androidx.annotation.RequiresApi;

import static java.lang.Integer.min;
import static java.lang.Integer.max;

public class DrawingView extends View {
    private Path drawPath;
    private Paint drawPaint, canvasPaint, pointPaint, alphaPaint, rectPaint;
    private int paintColor = Color.RED;
    private Canvas drawCanvas;
    private Bitmap smallBitmap;
    private Bitmap canvasBitmap;
    private Bitmap mask;
    private Bitmap original;
    private int minx, miny, width, height;
    private float touchX, touchY;
    private boolean isTouched;
    private boolean isRight;

    public DrawingView(Context context, Bitmap mask, Bitmap original) {
        super(context);
        this.mask = mask;
        this.original = original;
        setupDrawing();
    }

    private void setupDrawing() {
        isTouched = false;
        isRight = true;
        drawPath = new Path();
        drawPaint = new Paint();
        pointPaint = new Paint();
        alphaPaint = new Paint();
        rectPaint = new Paint();
        pointPaint.setColor(Color.LTGRAY);
        pointPaint.setStyle(Paint.Style.STROKE);
        pointPaint.setAntiAlias(true);
        pointPaint.setStrokeWidth(3);
        drawPaint.setStrokeWidth(3);
        drawPaint.setColor(paintColor);
        drawPaint.setAntiAlias(true);
        drawPaint.setStrokeWidth(50);
        drawPaint.setStyle(Paint.Style.STROKE);
        drawPaint.setStrokeJoin(Paint.Join.ROUND);
        drawPaint.setStrokeCap(Paint.Cap.ROUND);

        alphaPaint.setStrokeWidth(3);
        alphaPaint.setColor(paintColor);
        alphaPaint.setAntiAlias(true);
        alphaPaint.setStrokeWidth(50);
        alphaPaint.setStyle(Paint.Style.STROKE);
        alphaPaint.setStrokeJoin(Paint.Join.ROUND);
        alphaPaint.setStrokeCap(Paint.Cap.ROUND);
        alphaPaint.setAlpha(178);

        rectPaint.setColor(Color.BLACK);
        rectPaint.setAntiAlias(true);
        rectPaint.setStyle(Paint.Style.STROKE);
        rectPaint.setStrokeWidth(10);

        canvasPaint = new Paint(Paint.DITHER_FLAG);
        this.setDrawingCacheEnabled(true);
    }

    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        width = mask.getWidth();
        height = mask.getHeight();

        if ((float) width / (float) height > (float) w / (float) h) {
            height = height * w / width;
            width = w;
        } else {
            width = width * h / height;
            height = h;
        }
        minx = (w - width) / 2;
        miny = (h - height) / 2;
        Bitmap scaledBitmap = Bitmap.createScaledBitmap(getDrawableBitmap(), width, height, true);
        canvasBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        smallBitmap = Bitmap.createBitmap(200, 200, Bitmap.Config.ARGB_8888);
        drawCanvas = new Canvas(canvasBitmap);
        drawCanvas.drawBitmap(scaledBitmap, minx, miny, null);
    }

    public void setStrokeWidth(float val) {
        drawPaint.setStrokeWidth(val);
    }

    public void setBrush(boolean brush) {
        if (brush) {
            drawPaint.setColor(Color.RED);
            drawPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.ADD));
        } else {
            drawPaint.setColor(Color.BLACK);
            drawPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    protected void onDraw(Canvas canvas) {
        Bitmap tempBitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
        Bitmap tempOriginBitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
        Canvas tempCanvas = new Canvas(tempBitmap);
        Canvas tempOriginCanvas = new Canvas(tempOriginBitmap);
        Bitmap scaledOriginalBitmap = Bitmap.createScaledBitmap(original, width, height, true);

        tempOriginCanvas.drawBitmap(scaledOriginalBitmap, minx, miny, canvasPaint);
        tempCanvas.drawBitmap(canvasBitmap, 0, 0, canvasPaint);
        tempCanvas.drawPath(drawPath, drawPaint);

        canvas.drawBitmap(tempBitmap, 0, 0, canvasPaint);
        tempOriginCanvas.drawBitmap(tempBitmap, 0, 0, alphaPaint);

        if (isTouched) {
            canvas.drawCircle(touchX, touchY, drawPaint.getStrokeWidth() / 2, pointPaint);
            tempCanvas.drawCircle(touchX, touchY, drawPaint.getStrokeWidth() / 2, pointPaint);
            tempOriginCanvas.drawCircle(touchX, touchY, drawPaint.getStrokeWidth() / 2, pointPaint);

            Log.i("[drawableBitmap]", getWidth() + ", " + getHeight() + "");
            Log.i("[touchX, minX]", touchX + ", " + minx + "");
            Log.i("[touchY, minY]", touchY + ", " + miny + "");
            int xCoord = max((int) touchX, smallBitmap.getWidth() / 2);
            int yCoord = max((int) touchY, smallBitmap.getHeight() / 2);
            xCoord = min(xCoord, getWidth() - smallBitmap.getWidth() / 2 - 1);
            yCoord = min(yCoord, getHeight() - smallBitmap.getHeight() / 2 - 1);
            smallBitmap =
                    Bitmap.createBitmap(tempOriginBitmap, xCoord - smallBitmap.getWidth() / 2, yCoord - smallBitmap.getHeight() / 2, smallBitmap.getWidth(), smallBitmap.getHeight());
            Bitmap scaledSmallBitmap = Bitmap.createScaledBitmap(smallBitmap, 400, 400, true);


            if (isRight) {
                if (xCoord >= getWidth() * 0.7) isRight = false;
            } else {
                if (xCoord < getWidth() * 0.3) isRight = true;
            }

            if (isRight) {
                canvas.drawBitmap(scaledSmallBitmap, getWidth() - (float) scaledSmallBitmap.getWidth() - minx - rectPaint.getStrokeWidth(), 200, null);
                canvas.drawRect(new Rect(getWidth() - scaledSmallBitmap.getWidth() - minx - (int) rectPaint.getStrokeWidth(), 200, getWidth() - minx - (int) rectPaint.getStrokeWidth(), 200 + scaledSmallBitmap.getHeight()), rectPaint);
            } else {
                canvas.drawBitmap(scaledSmallBitmap, minx + rectPaint.getStrokeWidth(), 200, null);
                canvas.drawRect(new Rect(minx + (int) rectPaint.getStrokeWidth(), 200, minx + scaledSmallBitmap.getWidth() + (int) rectPaint.getStrokeWidth(), 200 + scaledSmallBitmap.getHeight()), rectPaint);
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        touchX = event.getX();
        touchY = event.getY();
//        float brushSize = drawPaint.getStrokeWidth() / 2;
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                drawPath.moveTo(touchX, touchY);
                isTouched = true;
                break;
            case MotionEvent.ACTION_MOVE:
                drawPath.lineTo(touchX, touchY);
                break;
            case MotionEvent.ACTION_UP:
                drawPath.lineTo(touchX, touchY);
                drawCanvas.drawPath(drawPath, drawPaint);
                drawPath.reset();
                isTouched = false;
                break;
            default:
        }

        invalidate();

        return true;
    }


    private Bitmap getDrawableBitmap() {
        Bitmap ret = Bitmap.createBitmap(mask.getWidth(), mask.getHeight(), Bitmap.Config.ARGB_8888);
        for (int x = 0; x < mask.getWidth(); x++) {
            for (int y = 0; y < mask.getHeight(); y++) {
                if (mask.getPixel(x, y) != Color.WHITE) ret.setPixel(x, y, Color.TRANSPARENT);
                else ret.setPixel(x, y, Color.RED);
            }
        }
        return ret;
    }

    public Bitmap getMask() {
        Bitmap ret = Bitmap.createBitmap(canvasBitmap, minx, miny, width, height);
        for (int x = 0; x < ret.getWidth(); x++) {
            for (int y = 0; y < ret.getHeight(); y++) {
                if (ret.getPixel(x, y) == 0) ret.setPixel(x, y, Color.TRANSPARENT);
                else ret.setPixel(x, y, Color.WHITE);
            }
        }
        ret = Bitmap.createScaledBitmap(ret, mask.getWidth(), mask.getHeight(), true);
        return ret;
    }
}