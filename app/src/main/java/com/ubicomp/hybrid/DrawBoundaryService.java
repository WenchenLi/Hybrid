package com.ubicomp.hybrid;

import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.os.IBinder;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

/**
 * Created by wenchen on 8/31/15.
 */
public class DrawBoundaryService extends Service {
//    private DrawBoundary db;
    private WindowManager windowManager;
    private ImageView chatHead;
    private String TAG = "DrawBoundaryService";
    @Override public IBinder onBind(Intent intent) {
        // Not used
        return null;
    }

    @Override public void onCreate() {
        super.onCreate();
        Log.v(TAG, "create Service");
        Toast.makeText(getApplicationContext(),
                R.string.Put_Object_within, Toast.LENGTH_SHORT).show();
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);

//        db = new DrawBoundary(this,200,200);
//        Bitmap b = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888);
//        Canvas c = new Canvas(b);
//        db.draw(c);

        chatHead = new ImageView(this);
        Bitmap bm = ImageProcessor.makeDst(2000, 3000);
        chatHead.setImageBitmap(bm);
//        chatHead.setImageResource(R.drawable.ic_launcher);

        final WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY,
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                PixelFormat.RGBA_8888);

        params.gravity = Gravity.CENTER_HORIZONTAL|Gravity.CENTER_VERTICAL;
        params.x = 0;
        params.y = 0;

        windowManager.addView(chatHead, params);
//        windowManager.addView(db,params);
//        db.bringToFront();

//        db.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Log.v(TAG,"remove Draw Boundary");
//                windowManager.removeViewImmediate(db);
//            }
//        });
        chatHead.setOnTouchListener(new View.OnTouchListener() {
            private int initialX;
            private int initialY;
            private float initialTouchX;
            private float initialTouchY;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        initialX = params.x;
                        initialY = params.y;
                        initialTouchX = event.getRawX();
                        initialTouchY = event.getRawY();
                        return true;
                    case MotionEvent.ACTION_UP:
                        return true;
                    case MotionEvent.ACTION_MOVE:
                        params.x = initialX + (int) (event.getRawX() - initialTouchX);
                        params.y = initialY + (int) (event.getRawY() - initialTouchY);
                        windowManager.updateViewLayout(chatHead, params);
                        return true;
                }
                return false;
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (chatHead != null) windowManager.removeView(chatHead);
    }
}
