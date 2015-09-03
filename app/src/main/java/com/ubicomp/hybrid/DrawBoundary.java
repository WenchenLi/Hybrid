package com.ubicomp.hybrid;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;
import android.view.SurfaceView;

/**
 * Created by wenchen on 9/1/15.
 */
public class DrawBoundary extends SurfaceView {
    private Paint paint;
    private int width;
    private int height;
    private int radius=100;

    public DrawBoundary(Context context, int w, int h)
    {
        super(context);
        width = w;
        height = h;
//        Canvas grid = new Canvas(Bitmap.createBitmap(h, w, Bitmap.Config.ARGB_8888));
//        grid. drawColor(Color.WHITE);
        paint = new Paint();
//        paint.setStyle(Paint.Style.FILL_AND_STROKE);
//        grid.drawCircle(w / 2, h / 2, radius, paint);
    }
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Log.v("DrawBoundary","onDraw");
        paint.setStyle(Paint.Style.STROKE);
        canvas.drawPaint(paint);
        width = getWidth();
        height = getHeight();
        // Use Color.parseColor to define HTML colors
        paint.setColor(Color.YELLOW);
        canvas.drawCircle(width/2, height/2, radius, paint);
    }

//    private void init(){
//        temp = new Canvas(bitmap);
//        Bitmap bitmap = Bitmap.createBitmap(canvas.getWidth(), canvas.getHeight(), Bitmap.Config.ARGB_8888);
//        paint = new Paint();
//        paint.setColor(0xcc000000);
//        transparentPaint = new Paint();
//        transparentPaint.setColor(getResources().getColor(android.R.color.transparent));
//        transparentPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
//    }
//    protected void onDraw(Canvas canvas) {
//        temp.drawRect(0, 0, temp.getWidth(), temp.getHeight(), paint);
//        temp.drawCircle(catPosition.x + radius / 2, catPosition.y + radius / 2, radius, transparentPaint);
//        canvas.drawBitmap(bitmap, 0, 0, p);
//    }
}


