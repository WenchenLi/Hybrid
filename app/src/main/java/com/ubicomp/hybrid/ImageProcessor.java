package com.ubicomp.hybrid;


import android.content.res.Resources;
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

    static Bitmap HybridTesttoBitmap(){
//        Mat far = new Mat();
//        Utils.bitmapToMat(BitmapFactory.decodeResource(Resources.getSystem(), R.drawable.test1),far);
//        Mat close = new Mat();
//        Utils.bitmapToMat(BitmapFactory.decodeResource(Resources.getSystem(), R.drawable.test1),close);
        String imageUri1 = "drawable://" + R.drawable.test1;
        String imageUri2= "drawable://" + R.drawable.test2;


        Mat far = Imgcodecs.imread(imageUri1);
        Mat close = Imgcodecs.imread(imageUri2);
        Bitmap bm = BitmapFactory.decodeResource(Resources.getSystem(),R.drawable.test1);

        Utils.matToBitmap(ImageProcessor.Hybridize(far, close),bm);
        return bm;

    }

}
