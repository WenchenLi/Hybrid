package com.ubicomp.hybrid;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Environment;
import android.util.Log;

import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.io.FileOutputStream;

import static org.opencv.core.Core.BORDER_REFLECT;
import static org.opencv.core.Core.add;
import static org.opencv.core.Core.subtract;

/**
 * Created by ave on 8/27/15.
 */
public class ImageProcessor {
    static final String TAG = "ImageProcessor";

    static Bitmap LowPass(Bitmap image, int cutoff_frequency){
        Mat lowFreq = new Mat(image.getHeight(),image.getWidth(),CvType.CV_8U);
        Utils.bitmapToMat(image, lowFreq);

        Imgproc.GaussianBlur(lowFreq, lowFreq,
                new Size(cutoff_frequency * 4 + 1, cutoff_frequency * 4 + 1),
                cutoff_frequency, cutoff_frequency, BORDER_REFLECT);
        Utils.matToBitmap(lowFreq, image);
        SaveBitMapImage(image,"lowFreq.jpg");
        return image;
    }
    static Bitmap HighPass(Bitmap image, int cutoff_frequency){
        Mat original = new Mat(image.getHeight(),image.getWidth(),CvType.CV_32F);
        Mat lowFreq = new Mat(image.getHeight(),image.getWidth(),CvType.CV_32F);
        Mat highFreq = new Mat(image.getHeight(),image.getWidth(),CvType.CV_32F);

        Utils.bitmapToMat(image, original);
        Imgproc.GaussianBlur(original, lowFreq,
                new Size(cutoff_frequency * 4 + 1, cutoff_frequency * 4 + 1),
                cutoff_frequency, cutoff_frequency, BORDER_REFLECT);
        subtract(original, lowFreq, highFreq);
        add(highFreq, new Scalar(127.5, 127.5, 127.5, 127.5), highFreq);
        highFreq.convertTo(highFreq, CvType.CV_8U);
        Utils.matToBitmap(highFreq, image);
        SaveBitMapImage(image, "highFreq.jpg");
        return image;
    }

    static Mat Hybridize(Mat far, Mat close, int cutoff_frequency){
        Mat highFreq = new Mat(close.height(),close.width(),CvType.CV_32F);
//        Log.d("HiFreq Depth", Integer.toString(highFreq.depth()));
        Mat lowFreq = new Mat(close.height(),close.width(),CvType.CV_32F);
        Mat closeLowFreq= new Mat(close.height(),close.width(),CvType.CV_32F);
        Mat hybrid = new Mat(close.height(),close.width(),CvType.CV_32F);

        Imgproc.GaussianBlur(far, lowFreq, new Size(cutoff_frequency * 4 + 1, cutoff_frequency * 4 + 1), cutoff_frequency, cutoff_frequency, BORDER_REFLECT);
        Imgproc.GaussianBlur(close, closeLowFreq, new Size(cutoff_frequency * 4 + 1, cutoff_frequency * 4 + 1), cutoff_frequency, cutoff_frequency, BORDER_REFLECT);
//        SaveMatImage(far, "far.bmp");
//        SaveMatImage(close, "close.bmp");
//        SaveMatImage(lowFreq, "lowFreq.bmp");
//        SaveMatImage(closeLowFreq, "closeLowFreq.bmp");

        subtract(close, closeLowFreq, highFreq);
//        SaveMatImage(highFreq,"highFreq.bmp");
        Mat highFreqVisualize = new Mat(close.height(),close.width(),CvType.CV_32F);
        add(highFreq, new Scalar(127.5,127.5,127.5,127.5), highFreqVisualize);
//        SaveMatImage(highFreqVisualize, "highFreqVisualize.bmp");
       // Canny(close, highFreq, 300, 600, 5, true);// or laplacian filter

        add(highFreq, lowFreq, hybrid);
        //convert back to cv8u
//        highFreq.convertTo(highFreq, CvType.CV_8U, 250);
//        SaveMatImage(hybrid,"hybrid.bmp");

        return hybrid;
    }

    static Mat HybridTest(){
        Mat far = Imgcodecs.imread("/res/drawable/test1.bmp");
        Mat close = Imgcodecs.imread("/res/drawable/test2.bmp");
        return ImageProcessor.Hybridize(far,close,7);
    }

    static Bitmap HybridTestToBitmap(Context context){
        Mat far = new Mat();
        Mat close = new Mat();
        Utils.bitmapToMat(BitmapFactory.decodeResource(context.getResources(), R.drawable.test1), far);
        Utils.bitmapToMat(BitmapFactory.decodeResource(context.getResources(), R.drawable.test2), close);
        far.convertTo(far, CvType.CV_32F);
        close.convertTo(close, CvType.CV_32F);
        Bitmap bm = BitmapFactory.decodeResource(context.getResources(), R.drawable.test2);
//        Log.v(TAG+"far type:", Integer.toString(far.type()));
//        Log.v(TAG + "close:", Integer.toString(close.type()));
        Mat hybrid = ImageProcessor.Hybridize(far, close,20);
        hybrid.convertTo(hybrid,CvType.CV_8U);
        Utils.matToBitmap(hybrid,bm);
        return bm;
    }

    static Bitmap HybridToBitmap(Context context, Bitmap first,Bitmap second){
        Mat far = new Mat(first.getHeight(),first.getWidth(),CvType.CV_32F);
        Mat close = new Mat(second.getHeight(),second.getWidth(),CvType.CV_32F);
        Utils.bitmapToMat(first, far);
        Utils.bitmapToMat(second, close);
//        Bitmap bm = BitmapFactory.decodeFile("file:///sdcard/first.bmp");

        Mat hybrid = ImageProcessor.Hybridize(far, close,20);
        hybrid.convertTo(hybrid,CvType.CV_8U);
        SaveMatImage(hybrid, "hybrid.jpg");
        Utils.matToBitmap(hybrid,first);

        return first;
    }
    static private void SaveMatImage (Mat mat,String filename) {
        Mat mIntermediateMat = new Mat();

        Imgproc.cvtColor(mat, mIntermediateMat, Imgproc.COLOR_BGR2RGB, 3);

        File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        File file = new File(path, filename);

        Boolean bool = null;
        filename = file.toString();
        bool = Imgcodecs.imwrite(filename, mIntermediateMat);

        if (bool == true)
            Log.d(TAG, "SUCCESS writing image to external storage");
        else
            Log.d(TAG, "Fail writing image to external storage");
    }

    static void SaveBitMapImage(Bitmap finalBitmap,String imageName) {

        File root = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
//        File myDir = new File(root + "/Pictures");
//        myDir.mkdirs();
//        Random generator = new Random();
//        int n = 10000;
//        n = generator.nextInt(n);
//        String fname = "Image-"+ n +".jpg";
        File file = new File (root, imageName);
        if (file.exists ()) file.delete();
        try {
            FileOutputStream out = new FileOutputStream(file);
            finalBitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
            out.flush();
            out.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    static  Mat LoadMatImage(String filename) {
        Mat IntermediateMat = new Mat();
        File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        File file = new File(path, filename);
        filename = file.toString();
        IntermediateMat = Imgcodecs.imread(filename);
        return IntermediateMat;
    }
    static  Bitmap LoadBitmapImage(String filename) {
        File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        File file = new File(path, filename);
        filename = file.toString();
        Bitmap bm = BitmapFactory.decodeFile(filename);
        return bm;
    }
    static Bitmap makeDst(int w, int h) {
        Bitmap bm = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(bm);
        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        p.setColor(Color.argb(90,200,200,0));
        c.drawOval(new RectF(0, 0, w * 3 / 4, h * 3 / 4), p);
        return bm;
    }

    static Bitmap overlay(Bitmap lowFreq, Bitmap highFreq) {
        Bitmap bmOverlay = Bitmap.createBitmap(lowFreq.getWidth(), lowFreq.getHeight(), lowFreq.getConfig());
        Mat highFreqMat = new Mat(lowFreq.getHeight(),lowFreq.getWidth(),CvType.CV_8U);
        Mat lowFreqMat = new Mat(lowFreq.getHeight(),lowFreq.getWidth(),CvType.CV_8U);
        Mat hybrid = new Mat(lowFreq.getHeight(),lowFreq.getWidth(),CvType.CV_8U);
        Utils.bitmapToMat(lowFreq,lowFreqMat);
        Utils.bitmapToMat(highFreq,highFreqMat);
        add(lowFreqMat, highFreqMat, hybrid);
        Utils.matToBitmap(hybrid,bmOverlay);
        return bmOverlay;
    }
//    static private Mat Cv8Uto32f(){
//        Mat maskColor = new Mat( mBitmapPintar.getHeight(), mBitmapPintar.getWidth(), CvType.CV_8UC3);
//        Mat maskgris = new Mat( maskColor.rows(), maskColor.cols(), CvType.CV_8U );
//        Mat mask = new Mat( maskColor.rows(), maskColor.cols(), CvType.CV_32F);
//
//        Imgproc.cvtColor(maskColor, maskgris, Imgproc.COLOR_BGR2GRAY);
//        maskgris.convertTo(mask, CvType.CV_32F);
//        mask.convertTo(maskgris, CvType.CV_8U);
//        return
//    }
}
