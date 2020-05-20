

#include <jni.h>
#include <string>
#include <opencv2/imgproc.hpp>
#include <opencv2/opencv.hpp>
#include <opencv2/highgui.hpp>
#include <opencv2/highgui/highgui_c.h>
#include <opencv2/core.hpp>
#include <android/log.h>
#include "masked_image.h"
#include "nnf.h"
#include "inpaint.h"
#include "mask_corrector.h"
#include "size_changer.h"


#define LOGI(str) __android_log_print(ANDROID_LOG_INFO,"mask corrector",str,__FUNCTION__);


extern "C" JNIEXPORT jstring JNICALL
Java_capstone_aiimageeditor_StartActivity_stringFromJNI(
        JNIEnv* env,
        jobject /* this */) {
    std::string hello = "Hello from C++";

    return env->NewStringUTF(hello.c_str());
}

extern "C"
JNIEXPORT void JNICALL
Java_capstone_aiimageeditor_MainActivity_startInpaint(JNIEnv *env, jobject thiz, jlong InputImage,
                                                      jlong maskPtr) {
    cv::Mat image;
    cv::Mat* source;
    cv::Mat *maskImage;


    source = (cv::Mat*) InputImage;
    maskImage = (cv::Mat*) maskPtr;


    Mat mask = cv::Mat(source->size(), CV_8UC1);
    mask = cv::Scalar::all(0);
    for (int i = 0; i < source->size().height; ++i) {
        for (int j = 0; j < source->size().width; ++j) {
            auto ptr = maskImage->ptr<unsigned char>(i,j);
            if(ptr[0]>0 && ptr[1]>0 && ptr[2]>0){
                mask.at<unsigned char>(i, j) = 255;
            }
        }
    }

    SizeChanger sc = SizeChanger(*source,mask);
    sc.getDownSized(source,maskImage);
    *source = Inpainting(*source,*maskImage, 2).run(true);
    *source = sc.getMerged(*source);
}
extern "C"
JNIEXPORT void JNICALL
Java_capstone_aiimageeditor_MainActivity_runMaskCorrector(JNIEnv *env, jobject thiz, jlong imagePtr,
                                                          jlong maskPtr) {
    cv::Mat* source;
    cv::Mat* mask;
    cv::Mat* ret;
    source = (cv::Mat*) imagePtr;
    mask = (cv::Mat*) maskPtr;
    MaskCorrector mc = MaskCorrector(*source,*mask);
    mc.Run();
    *mask = mc.getResult();
    //bitwise_and(*source,*source,*mask,*mask);
    //mask = ret;
}
