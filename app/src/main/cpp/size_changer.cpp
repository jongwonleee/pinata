//
// Created by 이종원 on 2020/05/14.
//

#include "size_changer.h"
#include "android/log.h"
#define LOGI(str) __android_log_print(ANDROID_LOG_INFO,"mask corrector",str,__FUNCTION__);

SizeChanger::SizeChanger(Mat source, Mat mask) {
    this->source=source;
    this->mask = mask;
    dMask = mask.clone();
    dSource = source.clone();

}

void SizeChanger::downsampling() {
    int max = (dSource.cols>dSource.rows)?dSource.cols:dSource.rows;
    while(max > 500){
        pyrDown(dSource,dSource,Size(dSource.cols/2,dSource.rows/2));
        pyrDown(dMask,dMask,Size(dMask.cols/2,dMask.rows/2));
        max = (dSource.cols>dSource.rows)?dSource.cols:dSource.rows;
        sampleRate*=2;
    }
}

Mat SizeChanger::upsampling(Mat result) {
    while(sampleRate>1){
        sampleRate/=2;
        pyrUp(result,result);
    }
    //pyrUp(result,result,Size(source.cols,source.rows));
    resize(result,result,Size(source.cols,source.rows),0,0,INTER_AREA);
    return result;
}

void SizeChanger::getDownSized(Mat* source, Mat* mask){
    downsampling();
    *source = this->dSource.clone();
    *mask = this->dMask.clone();
}
Mat SizeChanger::getMerged(Mat result){
    result = upsampling(result);
    for (int i = 0; i < source.size().height; ++i) {
        for (int j = 0; j < source.size().width; ++j) {
            auto ptr = mask.ptr<unsigned char>(i,j);
            if(ptr[0]>0 && ptr[1]>0 && ptr[2]>0){
                source.ptr<unsigned char>(i,j)[0] = result.ptr<unsigned char>(i,j)[0];
                source.ptr<unsigned char>(i,j)[1] = result.ptr<unsigned char>(i,j)[1];
                source.ptr<unsigned char>(i,j)[2] = result.ptr<unsigned char>(i,j)[2];
            } 
        }
    }

    return source;
}