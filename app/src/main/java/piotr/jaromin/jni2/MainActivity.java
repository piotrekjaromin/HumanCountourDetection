package piotr.jaromin.jni2;

import android.graphics.Bitmap;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceView;
import android.widget.TextView;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.JavaCameraView;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2{

    private static String TAG = "MainActicity";
    JavaCameraView javaCameraView;
    Mat mRgba, imgGray, imgCanny, hsv;

    private Mat                    mIntermediateMat;
    private Mat                    mGray;
    Mat hierarchy;

    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }

    BaseLoaderCallback mLoaderCallBack = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case BaseLoaderCallback.SUCCESS: {
                    javaCameraView.enableView();
                    break;
                }
                default:{
                    super.onManagerConnected(status);
                    break;
                }
            }
            super.onManagerConnected(status);
        }
    };

    static {
        if(OpenCVLoader.initDebug()) {
            Log.d(TAG, "Opencv successfull loaded");
        } else {
            Log.d(TAG, "Opencv not loaded");
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        javaCameraView = (JavaCameraView) findViewById(R.id.java_camera_view);
        javaCameraView.setVisibility(SurfaceView.VISIBLE);
        javaCameraView.setCvCameraViewListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(javaCameraView!=null) {
            javaCameraView.disableView();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(javaCameraView!=null) {
            javaCameraView.disableView();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(OpenCVLoader.initDebug()) {
            Log.d(TAG, "Opencv successfull loaded");
            mLoaderCallBack.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        } else {
            Log.d(TAG, "Opencv not loaded");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_2_0, this, mLoaderCallBack);
        }
    }


    @Override
    public void onCameraViewStarted(int width, int height) {
        mRgba = new Mat(height, width, CvType.CV_8UC4);

        imgGray = new Mat(height, width, CvType.CV_8UC1);
        imgCanny = new Mat(height, width, CvType.CV_8UC1);
        mRgba = new Mat(height, width, CvType.CV_8UC4);
        mIntermediateMat = new Mat(height, width, CvType.CV_8UC4);
        mGray = new Mat(height, width, CvType.CV_8UC1);
        hierarchy = new Mat();


    }

    @Override
    public void onCameraViewStopped() {
        mRgba.release();
    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        mRgba = inputFrame.rgba();
        hsv = new Mat();
        Imgproc.cvtColor(mRgba, hsv, Imgproc.COLOR_RGB2HSV, 1);

        Mat mMaskMat = new Mat();
        Mat mDilatedMat = new Mat();


        Scalar lowerThreshold = new Scalar ( 0, 0, 0 ); // Blue color – lower hsv values
        Scalar upperThreshold = new Scalar ( 0, 0, 255 ); // Blue color – higher hsv values
        Core.inRange ( hsv, lowerThreshold , upperThreshold, mMaskMat );

        Imgproc.dilate ( mMaskMat, mDilatedMat, new Mat() );

        List<MatOfPoint> contours = new ArrayList<MatOfPoint>();

        Imgproc.findContours ( mDilatedMat, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE );

        Scalar colorGreen = new Scalar ( 120, 255, 255 );

        for ( int contourIdx=0; contourIdx < contours.size(); contourIdx++ )
        {
            if(contours.get(contourIdx).size().area()>100) {
                Imgproc.drawContours ( mRgba, contours, contourIdx, colorGreen, 3);
            }

        }

        return mRgba;



//
//
//        List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
//
//        Imgproc.cvtColor( mRgba, hsv, Imgproc.COLOR_RGB2HSV);
//
//        Scalar lowerThreshold = new Scalar ( 120, 100, 100 ); // Blue color – lower hsv values
//        Scalar upperThreshold = new Scalar ( 179, 255, 255 ); // Blue color – higher hsv values
//        //Core.inRange ( hsv, lowerThreshold , upperThreshold, mMaskMat );
//
//
//        Imgproc.cvtColor(mRgba, imgGray, Imgproc.COLOR_RGB2GRAY);
//        Mat hierarchy = new Mat();
//        Imgproc.findContours(imgGray, contours, hierarchy, Imgproc.RETR_TREE,Imgproc.CHAIN_APPROX_SIMPLE);
//
//        for (int contourIdx = 0; contourIdx < contours.size(); contourIdx++) {
//            Imgproc.drawContours(mRgba, contours, contourIdx, new Scalar(0, 0, 255), -1);
//        }
//        Imgproc.Canny(imgGray, imgCanny, 50, 150);
        //mRgba = inputFrame.rgba();

//        mRgba = inputFrame.gray();
//        ArrayList<MatOfPoint> contours = new ArrayList<MatOfPoint>();
//        hierarchy = new Mat();
//
//        Imgproc.Canny(mRgba, mIntermediateMat, 80, 100);
//        Imgproc.findContours(mIntermediateMat, contours, hierarchy, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE, new Point(0, 0));
//    /* Mat drawing = Mat.zeros( mIntermediateMat.size(), CvType.CV_8UC3 );
//     for( int i = 0; i< contours.size(); i++ )
//     {
//    Scalar color =new Scalar(Math.random()*255, Math.random()*255, Math.random()*255);
//     Imgproc.drawContours( drawing, contours, i, color, 2, 8, hierarchy, 0, new Point() );
//     }*/
//        hierarchy.release();
//        Imgproc.drawContours(mRgba, contours, -1, new Scalar(Math.random()*255, Math.random()*255, Math.random()*255));//, 2, 8, hierarchy, 0, new Point());
//        // Imgproc.cvtColor(mIntermediateMat, mRgba, Imgproc.COLOR_GRAY2RGBA, 4);


    }

//    private void detectRegion() {
//
//        //get the image from gallery and change it into bitmap
//        Bitmap bmpTemp = originalImg.copy(Bitmap.Config.ARGB_8888, true);
//        Utils.bitmapToMat(bmpTemp, mRgbMat);
//        Imgproc.cvtColor(mRgbMat, mHsvMat, Imgproc.COLOR_RGB2HSV, channelCount);
//
//        Scalar lowerThreshold = new Scalar(0, 0.23 * 255, 50); // Blue color – lower hsv values
//        Scalar upperThreshold = new Scalar(50, 0.68 * 255, 255); // Blue color – higher hsv values
//        Core.inRange(mHsvMat, lowerThreshold, upperThreshold, mMaskMat);
//        Imgproc.dilate(mMaskMat, mDilatedMat, new Mat());
//
//        Imgproc.findContours(mMaskMat, contours, hierarchy, Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);
//
//        Imgproc.drawContours(mRgbMat, contours,counter, colorGreen, iLineThickness);
//        Log.d(TAG + " contours " , contours.get(counter).toString());
//
//        // convert to bitmap:
//        Bitmap bm = Bitmap.createBitmap(mHsvMat.cols(), mHsvMat.rows(), Bitmap.Config.ARGB_8888);
//        Utils.matToBitmap(mRgbMat, bm);
//
//        // find the imageview and draw it!
//        imageView.setImageBitmap(bm);
//    }



    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    public native String stringFromJNI();
    public native static int convertGray(long matAddrRgba, long matAddrGray);
}
