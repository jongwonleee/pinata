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



extern "C"
JNIEXPORT void JNICALL
Java_capstone_aiimageeditor_ImageManager_00024InpaintTask_startInpaint(JNIEnv *env, jobject thiz, jlong InputImage,
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
Java_capstone_aiimageeditor_ImageManager_startMaskCorrection(JNIEnv *env,
                                                                              jobject thiz,
                                                                              jlong sourceImage,
                                                                              jlong maskPtr) {
    cv::Mat image;
    cv::Mat* source;
    cv::Mat* mask;


    source = (cv::Mat*) sourceImage;
    mask = (cv::Mat*) maskPtr;
    MaskCorrector maskCorrector = MaskCorrector(*source,*mask);
    maskCorrector.Run();
    *mask = maskCorrector.getResult();
}