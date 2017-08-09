#include <jni.h>
#include <stdio.h>
#include <opencv2/opencv.hpp>

using namespace cv;
using namespace std;

extern "C" {
int toGray(Mat img, Mat& gray);

JNIEXPORT jint JNICALL
Java_piotr_jaromin_jni2_MainActivity_convertGray(
        JNIEnv *, jobject,jlong, jlong);
}