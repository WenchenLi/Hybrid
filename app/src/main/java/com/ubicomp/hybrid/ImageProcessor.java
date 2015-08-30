package com.ubicomp.hybrid;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.util.Log;

import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.io.File;

import static org.opencv.core.Core.BORDER_REFLECT;
import static org.opencv.core.Core.add;
import static org.opencv.core.Core.subtract;

/**
 * Created by ave on 8/27/15.
 */
public class ImageProcessor {
    static final String TAG = "ImageProcessor";

    static Mat Hybridize(Mat far, Mat close,int cutoff_frequency){
        Mat highFreq = new Mat(close.height(),close.width(),CvType.CV_32F);
        Mat lowFreq = new Mat(close.height(),close.width(),CvType.CV_32F);
        Mat closeLowFreq= new Mat(close.height(),close.width(),CvType.CV_32F);
        Mat hybrid = new Mat(close.height(),close.width(),CvType.CV_32F);

        Imgproc.GaussianBlur(far, lowFreq, new Size(cutoff_frequency*4+1, cutoff_frequency*4+1), cutoff_frequency,cutoff_frequency,BORDER_REFLECT);
        Imgproc.GaussianBlur(close, closeLowFreq, new Size(cutoff_frequency * 4 + 1, cutoff_frequency * 4 + 1), cutoff_frequency, cutoff_frequency,BORDER_REFLECT);
        SaveImage(far, "far.bmp");
        SaveImage(close,"close.bmp");


        SaveImage(lowFreq, "lowFreq.bmp");
        SaveImage(closeLowFreq, "closeLowFreq.bmp");

        subtract(close, closeLowFreq, highFreq);
        SaveImage(highFreq, "highFreq.bmp");

        add(highFreq, lowFreq, hybrid);
        SaveImage(hybrid,"hybrid.bmp");

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
        Log.v(TAG+"far type:", Integer.toString(far.type()));
        Log.v(TAG + "close:", Integer.toString(close.type()));
        Mat hybrid = ImageProcessor.Hybridize(far, close,7);
        hybrid.convertTo(hybrid,CvType.CV_8U);
        Utils.matToBitmap(hybrid,bm);
        return bm;
    }
    static private void SaveImage (Mat mat,String filename) {
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
    static private Mat LoadImage(String filename) {
        Mat mIntermediateMat = new Mat();
        File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        File file = new File(path, filename);
        filename = file.toString();
        mIntermediateMat = Imgcodecs.imread(filename);
        return mIntermediateMat;
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
