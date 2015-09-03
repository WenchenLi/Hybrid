package com.ubicomp.hybrid;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.FloatMath;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.Toast;

import org.opencv.android.OpenCVLoader;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.IOException;

import static com.ubicomp.hybrid.ImageProcessor.HighPass;
import static com.ubicomp.hybrid.ImageProcessor.LoadBitmapImage;
import static com.ubicomp.hybrid.ImageProcessor.LowPass;
import static com.ubicomp.hybrid.ImageProcessor.overlay;


public class MainActivity extends AppCompatActivity implements OnTouchListener {
    private ImageView mImageView;
    private Button mChoose;
    private Button mTake;
    private static  final String mStoragePath = Environment.getExternalStorageDirectory().getPath()+"/Hybrid/";

    static final int REQUEST_IMAGE_CAPTURE_LOW_PASS = 1;
    static final int REQUEST_IMAGE_CAPTURE_HIGH_PASS = 2;
    static final int CHOOSE_IMAGE_REQUEST = 3;
    private int filter_pass_option;
    private Intent mDrawBoundaryIntent;
    private Button mShare;
    private Switch mFilterSwitch;
    private static final String TAG = "MainActivity";
//    private static final float MIN_ZOOM = 1f,MAX_ZOOM = 1f;

    // These matrices will be used to scale points of the image
    Matrix matrix = new Matrix();
    Matrix savedMatrix = new Matrix();

    // The 3 states (events) which the user is trying to perform
    static final int NONE = 0;
    static final int DRAG = 1;
    static final int ZOOM = 2;
    int mode = NONE;

    // these PointF objects are used to record the point(s) the user is touching
    PointF start = new PointF();
    PointF mid = new PointF();
    float oldDist = 1f;
    private String selectedImagePath;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (!OpenCVLoader.initDebug()) {
            Log.v(TAG,"fail loading opencv");
            // Handle initialization error
        }

        mFilterSwitch =  (Switch) findViewById(R.id.filterSwitch);
        mFilterSwitch.setText("Low Pass filter on");
        filter_pass_option = REQUEST_IMAGE_CAPTURE_LOW_PASS;
        //attach a listener to check for changes in state
        mFilterSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView,
                                         boolean isChecked) {

                if (isChecked) {
                    filter_pass_option = REQUEST_IMAGE_CAPTURE_HIGH_PASS;
                    mFilterSwitch.setText("High Pass filter on");


                }
                else {
                    filter_pass_option = REQUEST_IMAGE_CAPTURE_LOW_PASS;
                    mFilterSwitch.setText("Low Pass filter on");
                }

            }
        });

        //check the current state before we display the screen


        //set face boundary
        mDrawBoundaryIntent = new Intent(getApplicationContext(), DrawBoundaryService.class);


        //TODO load the previous saved hybrid image as default in mImageView
        //ImageView
        mImageView = (ImageView) findViewById(R.id.imageView);

        //Bitmap bm = BitmapFactory.decodeResource(getResources(),R.drawable.backgrd);
        mImageView.setImageBitmap(null);//HybridTestToBitmap(this));

        Toast.makeText(getApplicationContext(),R.string.image_zoom,Toast.LENGTH_LONG).show();
        mImageView.setOnTouchListener(this);


        //Choose from existing gallery
        mChoose = (Button) findViewById(R.id.ChooseButton);
        mChoose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent,
                        "Select Picture"), CHOOSE_IMAGE_REQUEST);
//                Intent i = new Intent(Intent.ACTION_PICK,android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
//                startActivityForResult(i, CHOOSE_IMAGE_REQUEST);

            }
        });


        //Take picture from mobile camera
        mTake = (Button) findViewById(R.id.TakePictureButton);

        mTake.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(filter_pass_option == REQUEST_IMAGE_CAPTURE_LOW_PASS){
                    openCameraForResult(filter_pass_option,"first.bmp");
                }
                else openCameraForResult(filter_pass_option,"second.bmp");

            }
        });


        //TODO Share pic (maybe better gif to show the effect), so I guess we need a gif generator of two show the far and close view
        mShare = (Button) findViewById(R.id.ShareButton);
        mShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent share = new Intent(Intent.ACTION_SEND);
                share.setType("image/*");
                share.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(
                        new File(Environment
                                .getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),"hybrid.jpg")));
                startActivity(Intent.createChooser(share,"Share via"));


            }
        });
    }

    @Override
    public boolean onTouch(View v, MotionEvent event)
    {
        ImageView view = (ImageView) v;
        view.setScaleType(ImageView.ScaleType.MATRIX);
        float scale;

        dumpEvent(event);
        // Handle touch events here...

        switch (event.getAction() & MotionEvent.ACTION_MASK)
        {
            case MotionEvent.ACTION_DOWN:   // first finger down only
                savedMatrix.set(matrix);
                start.set(event.getX(), event.getY());
                Log.d(TAG, "mode=DRAG"); // write to LogCat
                mode = DRAG;
                break;

            case MotionEvent.ACTION_UP: // first finger lifted

            case MotionEvent.ACTION_POINTER_UP: // second finger lifted

                mode = NONE;
                Log.d(TAG, "mode=NONE");
                break;

            case MotionEvent.ACTION_POINTER_DOWN: // first and second finger down

                oldDist = spacing(event);
                Log.d(TAG, "oldDist=" + oldDist);
                if (oldDist > 5f) {
                    savedMatrix.set(matrix);
                    midPoint(mid, event);
                    mode = ZOOM;
                    Log.d(TAG, "mode=ZOOM");
                }
                break;

            case MotionEvent.ACTION_MOVE:

                if (mode == DRAG)
                {
                    matrix.set(savedMatrix);
                    matrix.postTranslate(event.getX() - start.x, event.getY() - start.y); // create the transformation in the matrix  of points
                }
                else if (mode == ZOOM)
                {
                    // pinch zooming
                    float newDist = spacing(event);
                    Log.d(TAG, "newDist=" + newDist);
                    if (newDist > 5f)
                    {
                        matrix.set(savedMatrix);
                        scale = newDist / oldDist; // setting the scaling of the
                        // matrix...if scale > 1 means
                        // zoom in...if scale < 1 means
                        // zoom out
                        matrix.postScale(scale, scale, mid.x, mid.y);
                    }
                }
                break;
        }

        view.setImageMatrix(matrix); // display the transformation on screen

        return true; // indicate event was handled
    }

    /*
     * --------------------------------------------------------------------------
     * Method: spacing Parameters: MotionEvent Returns: float Description:
     * checks the spacing between the two fingers on touch
     * ----------------------------------------------------
     */

    private float spacing(MotionEvent event)
    {
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return FloatMath.sqrt(x * x + y * y);
    }

    /*
     * --------------------------------------------------------------------------
     * Method: midPoint Parameters: PointF object, MotionEvent Returns: void
     * Description: calculates the midpoint between the two fingers
     * ------------------------------------------------------------
     */

    private void midPoint(PointF point, MotionEvent event)
    {
        float x = event.getX(0) + event.getX(1);
        float y = event.getY(0) + event.getY(1);
        point.set(x / 2, y / 2);
    }

    /** Show an event in the LogCat view, for debugging */
    private void dumpEvent(MotionEvent event)
    {
        String names[] = { "DOWN", "UP", "MOVE", "CANCEL", "OUTSIDE","POINTER_DOWN", "POINTER_UP", "7?", "8?", "9?" };
        StringBuilder sb = new StringBuilder();
        int action = event.getAction();
        int actionCode = action & MotionEvent.ACTION_MASK;
        sb.append("event ACTION_").append(names[actionCode]);

        if (actionCode == MotionEvent.ACTION_POINTER_DOWN || actionCode == MotionEvent.ACTION_POINTER_UP)
        {
            sb.append("(pid ").append(action >> MotionEvent.ACTION_POINTER_ID_SHIFT);
            sb.append(")");
        }

        sb.append("[");
        for (int i = 0; i < event.getPointerCount(); i++)
        {
            sb.append("#").append(i);
            sb.append("(pid ").append(event.getPointerId(i));
            sb.append(")=").append((int) event.getX(i));
            sb.append(",").append((int) event.getY(i));
            if (i + 1 < event.getPointerCount())
                sb.append(";");
        }

        sb.append("]");
        Log.d("Touch Events ---------", sb.toString());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    private void openCameraForResult(int requestCode,String picName){
//        startService(mDrawBoundaryIntent);
        Intent photo = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//        String path = Environment.getExternalStorageDirectory().getPath()+"/";
        Uri uri  = Uri.parse("file:///sdcard/" + picName);//"file:///sdcard/"+picName
        photo.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, uri);
        startActivityForResult(photo,requestCode);
    }


    public static  Bitmap crupAndScale (Bitmap source,int scale){
        int factor = source.getHeight() <= source.getWidth() ? source.getHeight(): source.getWidth();
        int longer = source.getHeight() >= source.getWidth() ? source.getHeight(): source.getWidth();
        int x = source.getHeight() >= source.getWidth() ?0:(longer-factor)/2;
        int y = source.getHeight() <= source.getWidth() ?0:(longer-factor)/2;
        source = Bitmap.createBitmap(source, x, y, factor, factor);
        source = Bitmap.createScaledBitmap(source, scale, scale, false);
        return source;
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // we apply our filters at here.
        // we should first ask user to choose from high pass filter or low pass filter to
        // apply to the result image, notice they can apply both to separate the image.
        // and then store the result in the local storage in (binary maybe
        //  to save storage bitmap maybe more efficient, Need to check). We let the user decide which two pic to be the hybrid.
        // We should have a browse storage separate view on left and right simutanuously for low frequency image
        // and high frequency image
        // feature we can have later: the object detection and
        // search the boundary of the image.
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_IMAGE_CAPTURE_LOW_PASS) {

                File file = new File(Environment.getExternalStorageDirectory().getPath(), "first.bmp");
                Uri uri = Uri.fromFile(file);
                Bitmap bitmap;
                try {
                    bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                    bitmap = crupAndScale(bitmap, 450); // if you mind scaling
                    bitmap = LowPass(bitmap,10);//TODO async
                    mImageView.setImageBitmap(bitmap);
//                    openCameraForResult(REQUEST_IMAGE_CAPTURE_HIGH_PASS, "second.bmp");//TODO  later worry about 08-31 12:06:40.450  14320-14320/com.ubicomp.hybrid E/ActivityThread﹕ Performing stop of activity that is not resumed: {com.ubicomp.hybrid/com.ubicomp.hybrid.MainActivity}

                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            } else if (requestCode == REQUEST_IMAGE_CAPTURE_HIGH_PASS) {
                    try {
                        stopService(mDrawBoundaryIntent);
                    } catch (NullPointerException e) {

                    }
                    File file = new File(Environment.getExternalStorageDirectory().getPath(), "second.bmp");
                    Uri uri = Uri.fromFile(file);
                    Bitmap bitmap2;
                    try {
                        bitmap2 = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                        bitmap2 = crupAndScale(bitmap2, 450); // if you mind scaling
                        bitmap2 = HighPass(bitmap2, 10);//TODO async
//                        SaveBitMapImage(bitmap2,"highPass.jpg");

//                        File file1 = new File(Environment.getExternalStorageDirectory().getPath(), "first.bmp");
//                        Uri uri1 = Uri.fromFile(file1);
//                        Bitmap bitmap1 = MediaStore.Images.Media.getBitmap(getContentResolver(), uri1);
//                        bitmap1 = crupAndScale(bitmap1, 450); // if you mind scaling
//                        Bitmap hybrid = HybridToBitmap(this, bitmap1, bitmap2);

                        Bitmap bitmap1 = LoadBitmapImage("lowFreq.jpg");
                        bitmap2  = overlay(bitmap1,bitmap2);
//                        SaveBitMapImage(bitmap2, "Hybrid.jpg");
                        mImageView.setImageBitmap(bitmap2);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
             else if (requestCode == CHOOSE_IMAGE_REQUEST) {
                Uri selectedImageUri = data.getData();
                if (Build.VERSION.SDK_INT < 19) {
                    selectedImagePath = getPath(selectedImageUri);
                    Bitmap bitmap = BitmapFactory.decodeFile(selectedImagePath);
                    if(filter_pass_option == REQUEST_IMAGE_CAPTURE_LOW_PASS) {
                        mImageView.setImageBitmap(LowPass(bitmap, 15));// TODO async
                    }
                    else {

                        Bitmap bitmap1 = LoadBitmapImage("lowFreq.jpg");
                        bitmap1 = crupAndScale(bitmap1, 450);
                        bitmap = crupAndScale(bitmap, 450);
                        bitmap  = overlay(bitmap1, bitmap);
                        mImageView.setImageBitmap(HighPass(bitmap, 15));// TODO async

                    }


                } else {
                    ParcelFileDescriptor parcelFileDescriptor;
                    try {
                        parcelFileDescriptor = getContentResolver().openFileDescriptor(selectedImageUri, "r");
                        FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();
                        Bitmap bitmap = BitmapFactory.decodeFileDescriptor(fileDescriptor);
                        parcelFileDescriptor.close();
                        if(filter_pass_option == REQUEST_IMAGE_CAPTURE_LOW_PASS) {
                            mImageView.setImageBitmap(LowPass(bitmap, 15));// TODO async
                        }
                        else {


                            Bitmap bitmap1 = LoadBitmapImage("lowFreq.jpg");
                            bitmap1 = crupAndScale(bitmap1, 450);
                            bitmap = crupAndScale(bitmap, 450);
                            bitmap  = overlay(bitmap1, bitmap);
                            mImageView.setImageBitmap(HighPass(bitmap, 15));// TODO async

                        }
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            else {
                Log.v(TAG, "onActivityResult else");
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.v(TAG,"onResume");
        overridePendingTransition(0, 0);
        try{
            stopService(mDrawBoundaryIntent);
        }catch (NullPointerException e){

        }
    }

    @Override
    public void onRestart() {
        super.onRestart();
        try{
            stopService(mDrawBoundaryIntent);
        }catch (NullPointerException e){

        }

    }
    @Override
    public void onPause() {
        super.onPause();

    }
    @Override
    public void onStop(){
        super.onStop();
    }
    @Override
    public void onDestroy(){
        super.onDestroy();

    }
    public String getPath(Uri uri) {
        if( uri == null ) {
            return null;
        }
        String[] projection = { MediaStore.Images.Media.DATA };
        Cursor cursor = getContentResolver().query(uri, projection, null, null, null);
        if( cursor != null ){
            int column_index = cursor
                    .getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        }
        return uri.getPath();
    }
    //    private void dispatchTakePictureIntent() {
//        Log.v(TAG,"writing to external storage");
//        Uri imageUri = Uri.fromFile(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES));
//        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
//            takePictureIntent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT,
//                    imageUri);
//            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE_LOW_PASS);
//        }
//    }
}
