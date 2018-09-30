package com.dds.textrecognition;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.dds.textrecognition.Model.Points;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;


public class DrawingView extends View {
    private static final float TOUCH_TOLERANCE = 4;
    private Paint mPaint;
    private Bitmap mBitmap;
    private Canvas mCanvas;
    private Path mPath;
    private Paint mBitmapPaint;
    private float mX, mY;
    private int width, height;
    Context context;
    Points points;
    Date startDate, endDate;


    public DrawingView(Context c) {
        super(c);
        points = new Points();
        context = c;
        mPath = new Path();
        mBitmapPaint = new Paint(Paint.DITHER_FLAG);

        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setDither(true);
        mPaint.setColor(Color.GREEN);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setStrokeWidth(15);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        width = w;
        height = h;

        mBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        mCanvas = new Canvas(mBitmap);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.drawBitmap(mBitmap, 0, 0, mBitmapPaint);
        canvas.drawPath(mPath,  mPaint);
    }


    private void touch_start(float x, float y) {
        mPath.reset();
        mPath.moveTo(x, y);
        mX = x;
        mY = y;

        mCanvas.drawCircle(x, y, 10f, mPaint);
    }

    private void touch_move(float x, float y) {
        float dx = Math.abs(x - mX);
        float dy = Math.abs(y - mY);

        if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
            mPath.quadTo(mX, mY, (x + mX)/2, (y + mY)/2);
            mX = x;
            mY = y;
        }
    }

    private void touch_up() {
        mPath.lineTo(mX, mY);
        // commit the path to our offscreen
        mCanvas.drawPath(mPath,  mPaint);
        // kill this so we don't double draw
        mPath.reset();
    }

    public void clearDraw () {
        saveAsPicture();
        mBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        mCanvas = new Canvas(mBitmap);
        mPath = new Path();
        invalidate();
        InkData.clearAll();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mPaint.setStyle(Paint.Style.FILL);
                touch_start(x, y);
                invalidate();
                startDate = Calendar.getInstance().getTime();
                points.add((int)x, (int)y, 0);
                break;
            case MotionEvent.ACTION_MOVE:
                mPaint.setStyle(Paint.Style.STROKE);
                touch_move(x, y);
                invalidate();
                endDate = Calendar.getInstance().getTime();
                long diff = endDate.getTime() - startDate.getTime();
                Log.e("TimeStamp", "" + diff);
                points.add((int)x, (int)y, (int) diff);
                break;
            case MotionEvent.ACTION_UP:
                touch_up();
                invalidate();
                //one stroke is complete
                InkData.addPointsToStroke(points.getxList());
                InkData.addPointsToStroke(points.getyList());
                InkData.addPointsToStroke(points.getTimeStampList());
                InkData.addStrokeToInk();
                points = new Points();
                break;
        }
        return true;
    }

    private void saveAsPicture () {

    }


}