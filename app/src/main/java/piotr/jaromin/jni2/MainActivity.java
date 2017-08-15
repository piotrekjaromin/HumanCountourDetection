package piotr.jaromin.jni2;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceView;
import android.view.Window;
import android.view.WindowManager;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.JavaCameraView;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;
import org.opencv.video.Video;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity  implements CameraBridgeViewBase.CvCameraViewListener2 {

    private static String TAG = "MainActicity";
    JavaCameraView javaCameraView;
    Mat mRgba;


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
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);

        javaCameraView = (JavaCameraView) findViewById(R.id.java_camera_view);
        javaCameraView.setVisibility(SurfaceView.VISIBLE);
        javaCameraView.setCvCameraViewListener(this);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
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

    }

    @Override
    public void onCameraViewStopped() {
        mRgba.release();
    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        mRgba = inputFrame.rgba();

        return findCountour(mRgba);

    }

    public Mat findCountour(Mat mRgba) {
        Mat hsv = new Mat();
        Mat hierarchy = new Mat();
        Imgproc.cvtColor(mRgba, hsv, Imgproc.COLOR_RGB2HSV, 1);

        Mat mMaskMat = new Mat();
        Mat mDilatedMat = new Mat();


        Scalar lowerThreshold = new Scalar ( 0, 0, 0 ); // Blue color – lower hsv values
        Scalar upperThreshold = new Scalar ( 180,255,30 ); // Blue color – higher hsv values
        Core.inRange ( hsv, lowerThreshold , upperThreshold, mMaskMat );

        Imgproc.dilate ( mMaskMat, mDilatedMat, new Mat() );

        List<MatOfPoint> contours = new ArrayList<MatOfPoint>();

        Imgproc.findContours ( mDilatedMat, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE );

        Scalar colorGreen = new Scalar ( 180,255,30 );

        for ( int contourIdx=0; contourIdx < contours.size(); contourIdx++ ) {
            if (contours.get(contourIdx).size().area() > 500) {
                Imgproc.drawContours(mRgba, contours, contourIdx, colorGreen, 2);
            }

        }

        return mRgba;

    }
}