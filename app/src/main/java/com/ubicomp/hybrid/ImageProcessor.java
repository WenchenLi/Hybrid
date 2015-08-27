package com.ubicomp.hybrid;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import static org.opencv.core.Core.absdiff;
import static org.opencv.core.Core.add;
/**
 * Created by ave on 8/27/15.
 */
public class ImageProcessor {

    static Mat Hybridize(Mat far, Mat close){
        Mat highFreq = new Mat();
        Mat lowFreq = new Mat();
        Mat closeLowFreq= new Mat();
        Mat hybrid = new Mat();

        Imgproc.GaussianBlur(far, lowFreq, new Size(7, 7), 2);
        Imgproc.GaussianBlur(close, closeLowFreq, new Size(7, 7), 2);

        absdiff(close, closeLowFreq, highFreq);
        add(highFreq, lowFreq, hybrid);

        return hybrid;
    }

    static Mat HybridTest(){
        Mat far = Imgcodecs.imread("/res/drawable/test1.bmp");
        Mat close = Imgcodecs.imread("/res/drawable/test2.bmp");
        return ImageProcessor.Hybridize(far,close);
    }

    static Bitmap HybridTesttoBitmap(Context context){
        Mat far = new Mat();
        Mat close = new Mat();
        Utils.bitmapToMat(BitmapFactory.decodeResource(context.getResources(), R.drawable.test1),far);
        Utils.bitmapToMat(BitmapFactory.decodeResource(context.getResources(), R.drawable.test2),close);
        Bitmap bm = BitmapFactory.decodeResource(context.getResources(),R.drawable.test2);
        Utils.matToBitmap(ImageProcessor.Hybridize(far, close),bm);
        return bm;
    }
}
