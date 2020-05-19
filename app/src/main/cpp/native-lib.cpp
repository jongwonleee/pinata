

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

#include "enhance.h"
#include "filters.h"
#include <opencv2/imgproc/types_c.h>

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



extern "C"
JNIEXPORT void JNICALL
Java_capstone_aiimageeditor_PhotoProcessing_nativeApplyFilter(JNIEnv *env, jclass clazz, jint mode,
                                                              jint val, jlong inp_addr,
                                                              jlong out_addr) {
    Mat &src = *(Mat*)inp_addr;
    Mat &dst = *(Mat*)out_addr;

    switch (mode) {
        case 0:
            dst = src.clone();
            break;
        case 1:
            applySajuno(src,dst,val);
            break;
        case 2:
            applyManglow(src, dst, val);
            break;
        case 3:
            applyPalacia(src, dst, val);
            break;
        case 4:
            applyAnax(src, dst, val);
            break;
        case 5:
            applySepia(src, dst, val);
            break;
        case 6:
            applyCyano(src, dst, val);
            break;
        case 7:
            applyBW(src, dst, val);
            break;
        case 8:
            applyAnsel(src, dst, val);
            break;
        case 9:
            applyGrain(src, dst, val);
            break;
        case 10:
            applyHistEq(src, dst, val);
            break;
        case 11:
            applyThreshold(src, dst, val);
            break;
        case 12:
            applyNegative(src,dst,val);
            break;
        case 13:
            applyGreenBoostEffect(src,dst,val);
            break;
        case 14:
            applyBoostRedEffect(src,dst,val);
            break;
        case 15:
            applyBlueBoostEffect(src,dst,val);
            break;
        case 16:
            applyColorBoostEffect(src,dst,val);
            break;
        case 17:
            applyCyanise(src,dst,val);
            break;
        case 18:
            applyFade(src,dst,val);
            break;
        case 19:
            applyCartoon(src, dst, val);
            break;
        case 20:
            applyEdgify(src, dst, val);
            break;
        case 21:
            applyPencilSketch(src, dst, val);
            break;
        case 22:
            applyRedBlueEffect(src,dst,val);
            break;
        case 23:
            applyRedGreenFilter(src,dst,val);
            break;
        case 24:
            applyWhiteYellowTint(src, dst, val);
            break;
        default:

            int lowThreshold = val;
            int ratio = 3;
            int kernel_size = 3;

            Mat grey, detected_edges;

            cvtColor(src, grey, CV_BGR2GRAY);

            blur(grey, detected_edges, Size(3, 3));
            dst.create(grey.size(), grey.type());

            Canny(detected_edges, detected_edges, lowThreshold, lowThreshold * ratio,
                  kernel_size);

            dst = Scalar::all(0);

            detected_edges.copyTo(dst, detected_edges);
            break;
    }
    for(int i=0;i<dst.rows;i++){
        dst.ptr<unsigned int>(5,i)[0]=255;
        dst.ptr<unsigned int>(5,i)[2]=255;
        dst.ptr<unsigned int>(5,i)[1]=255;
    }
}

extern "C"
JNIEXPORT void JNICALL
Java_capstone_aiimageeditor_PhotoProcessing_nativeEnhanceImage(JNIEnv *env, jclass clazz, jint mode,
                                                               jint val, jlong inp_addr,
                                                               jlong out_addr) {
    Mat &src = *(Mat*)inp_addr;
    Mat &dst = *(Mat*)out_addr;

    switch (mode){
        case 0:
            adjustBrightness(src, dst, val);
            break;
        case 1:
            adjustContrast(src, dst, val);
            break;
        case 2:
            adjustHue(src, dst, val);
            break;
        case 3:
            adjustSaturation(src, dst, val);
            break;
        case 4:
            adjustTemperature(src, dst, val);
            break;
        case 5:
            adjustTint(src, dst, val);
            break;
        case 6:
            adjustVignette(src, dst, val);
            break;
        case 7:
            adjustSharpness(src, dst, val);
            break;
        case 8:
            adjustBlur(src, dst, val);
            break;
        case 9:
            applyGammaEffect(src, dst, val);
            break;
        default:
            break;
    }

}

