//
// Created by 이종원 on 2020/05/02.
//

#include "mask_corrector.h"
#include <android/log.h>

#define LOGI(str) __android_log_print(ANDROID_LOG_INFO,"mask corrector",str,__FUNCTION__);

MaskCorrector::MaskCorrector(Mat source, Mat mask){
    cvtColor(source,this->source,COLOR_BGR2GRAY);
    this->result = Mat(source.size(),CV_8UC1);
    this->edge =  Mat(source.size(),CV_8UC1);
    this->mask = mask;
    threshold(mask,result,120,255,THRESH_BINARY);
 this->result = mask.clone(); //= cv::Scalar::all(0);
    /* for (int i = 0; i < mask.size().height; ++i) {
         for (int j = 0; j < mask.size().width; ++j) {
             auto ptr = mask.ptr<unsigned char>(i,j);
             if(!isBlack(ptr)) this->result.at<unsigned char>(i, j) = 255;
         }
     }*/

    CHECK_SIZE = source.size().width;
    leftEdges= vector<pair<int,int>>();
    rightEdges= vector<pair<int,int>>();
}

void MaskCorrector::detectMaskEdges() {
    leftEdges.clear();
    rightEdges.clear();
    for (int i = 0; i < source.size().height; i++) {
        for (int j = 0; j < source.size().width-1; j++) {
            bool blackNow = isBlack(result.ptr<unsigned char>(i,j));
            bool blackNext = isBlack(result.ptr<unsigned char>(i,j+1));
            if(blackNow && !blackNext) {
                leftEdges.push_back(make_pair(i,j));
            }
            if(!blackNow && blackNext) {
                rightEdges.push_back(make_pair(i,j));
            }
        }
    }
}

void MaskCorrector::detectSharpMaskEdges() {
    leftEdges.clear();
    rightEdges.clear();

    for (int i = 0; i < source.size().height; i++) {
        for (int j = 0; j < source.size().width-1; j++) {
            bool blackNow = isBlack(result.ptr<unsigned char>(i,j));
            bool blackNext = isBlack(result.ptr<unsigned char>(i,j+1));
            if(blackNow^blackNext && checkNear(i,j,1)==INT_MAX){
                if(blackNow) {
                    leftEdges.push_back(make_pair(i,j));
                    LOGI("count left");
                }else {
                    rightEdges.push_back(make_pair(i,j));
                    LOGI("count right");
                }
            }
        }
    }
}

void MaskCorrector::detectSourceEdges(int min,int max) {
    Mat cannyEdge;
    Canny(source,cannyEdge,min,max);
    edge = cv::Mat(source.size(), CV_8UC1);
    threshold(cannyEdge,edge,127,255,THRESH_BINARY);
}
void MaskCorrector::Run(){
    detectMaskEdges();
    int  max;
    //TODO 정확히 threshold 값 알아오기
    max = 200;//(source.size().width<source.size().height)?source.size().height:source.size().width;
    detectSourceEdges(max,max);
    int count=0;
    while(!leftEdges.empty() && !rightEdges.empty()){
        count = leftEdges.size() + rightEdges.size();
        detectSourceEdges(max,max);
        vector<pair<int,int>> newLeftEdges = vector<pair<int,int>>();
        vector<pair<int,int>> newRightEdges = vector<pair<int,int>>();

        while(!leftEdges.empty()){
            auto p = leftEdges.back();
            leftEdges.pop_back();
            if(!setMask(p.first,p.second,true)) newLeftEdges.push_back(p);
        }
        while(!rightEdges.empty()){
            auto p = rightEdges.back();
            rightEdges.pop_back();
            if(!setMask(p.first,p.second,false)) newRightEdges.push_back(p);
        }
        leftEdges = newLeftEdges;
        rightEdges = newRightEdges;
        max/=2;
        if(count == leftEdges.size() + rightEdges.size()){
            break;
        }
    }
    Mat element = Mat(result.size().width/100,result.size().height/100,CV_8U,Scalar(1));
    morphologyEx(result,result,MORPH_CLOSE,element);
    morphologyEx(result,result,MORPH_OPEN,element);

}

void MaskCorrector::color(int y,int x){
    for(int i=y-5;i<y+5;i++){
        for(int j=x-5;j<x+5;j++){
            result.ptr<unsigned char>(i,j)[0]=255;
            result.ptr<unsigned char>(i,j)[1]=0;
            result.ptr<unsigned char>(i,j)[2]=0;

        }
    }
}

int MaskCorrector::checkNear(int y, int x,int size) {
    for(int i = y-size;i<=y+size;i++){
        if( i<0 || i>=result.size().height ||i==y) continue;
        for(int j=x-size;j<=x+size;j++){
            if( j<0 || j>=result.size().width) continue;
            bool black = isBlack(result.ptr<unsigned char>(i,j));
            if(!black) {
                return i*i+j*j;
            }
        }
    }
    return INT_MAX;
}

bool MaskCorrector::setMask(int y,int x, bool isLeft){
    for(int k=0;k<CHECK_SIZE;k++){
        int left=INT_MAX;
        int right=INT_MAX;
        if(x+k<source.size().width && !isBlack(edge.ptr<unsigned char>(y,x+k))){
            left = checkNear(y,x+k,1);
        }
        if(x-k>=0 && !isBlack(edge.ptr<unsigned char>(y,x-k))){
            right= checkNear(y,x-k,1);
        }
        if(left==INT_MAX && right==INT_MAX) continue;
        if(left<right)
        {
            LOGI("filling left");
            for(int l =0;l<=k;l++){
                result.ptr<unsigned char>(y,x+l)[0]=(isLeft)?0:255;
                result.ptr<unsigned char>(y,x+l)[1]=(isLeft)?0:255;
                result.ptr<unsigned char>(y,x+l)[2]=(isLeft)?0:255;
            }
            return true;
        } else{
            LOGI("filling right");
            for(int l=0;l<=k-1;l++){
                result.ptr<unsigned char>(y,x-l)[0]=(isLeft)?255:0 ;
                result.ptr<unsigned char>(y,x-l)[1]=(isLeft)?255:0;
                result.ptr<unsigned char>(y,x-l)[2]=(isLeft)?255:0;
            }
            return true;
        }
    }
    return false;
}
bool MaskCorrector::setMaskByNear(int y,int x, bool isLeft){
    for(int k=0;k<CHECK_SIZE;k++){
        int left=INT_MAX;
        int right=INT_MAX;
        if(x+k<source.size().width){
            left = checkNear(y,x+k,1);
        }
        if(x-k>=0){
            right= checkNear(y,x-k,1);
        }
        if(left==INT_MAX && right==INT_MAX) continue;
        if(left<right)
        {
            LOGI("filling left near");
            for(int l = 0;l<=k+30;l++)
                result.at<unsigned char>(y,(x+l)*4)=120;//(isLeft)?0:255;
            return true;
        } else{
            LOGI("filling right near");
            for(int l = 0;l<=k;l++)
                result.at<unsigned char>(y,(x-l)*4)=120;//(isLeft)?255:0;
            return true;
        }
    }
    return false;
}

bool MaskCorrector::isBlack(uchar * ptr){
    return ptr[0]<10 && ptr[1]<10 && ptr[2]<10;
}
Mat MaskCorrector::getResult() {
    return result;
}
