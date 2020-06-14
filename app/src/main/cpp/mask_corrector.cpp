
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
    this->result = mask.clone();

    CHECK_SIZE = source.size().width/5;
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

void MaskCorrector::detectSourceEdges(int min,int max) {
    Mat cannyEdge;
    Canny(source,cannyEdge,min,max);
    edge = cv::Mat(source.size(), CV_8UC1);
    threshold(cannyEdge,edge,127,255,THRESH_BINARY);
}

void MaskCorrector::Run(){
    detectMaskEdges();
    int  max;
    max = 200;
    detectSourceEdges(max,max);
    while(!leftEdges.empty() && !rightEdges.empty()){
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
        if(max< 15) break;

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
            LOGI("filling left")
            for(int l =0;l<=k;l++){
                result.ptr<unsigned char>(y,x+l)[0]=(isLeft)?0:255;
                result.ptr<unsigned char>(y,x+l)[1]=(isLeft)?0:255;
                result.ptr<unsigned char>(y,x+l)[2]=(isLeft)?0:255;
            }
            return true;
        } else{
            LOGI("filling right")
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

bool MaskCorrector::isBlack(uchar * ptr){
    return ptr[0]<10 && ptr[1]<10 && ptr[2]<10;
}

Mat MaskCorrector::getResult() {
    return result;
}
/*

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
    this->result = mask.clone();

    CHECK_SIZE = source.size().width/5;
    leftEdges= vector<pair<int,int>>();
    rightEdges= vector<pair<int,int>>();
}

void MaskCorrector::detectMaskEdges() {
    leftEdges.clear();
    rightEdges.clear();
    for (int i = 0; i < source.size().height; i++) {
        for (int j = 0; j < source.size().width; j++) {
            bool hasRight = source.size().width>=j+1;
            bool hasBelow = source.size().height>=j+1;
            bool blackNow = isBlack(result.ptr<unsigned char>(i,j));
            bool blackRight = (hasRight)?isBlack(result.ptr<unsigned char>(i,j+1)): false;
            bool blackBelow = (hasBelow)?isBlack(result.ptr<unsigned char>(i+1,j)):false;
            if(hasRight &&blackNow && !blackRight) {
                leftEdges.push_back(make_pair(i,j));
            }
            if(hasRight &&!blackNow && blackRight) {
                rightEdges.push_back(make_pair(i,j));
            }
            if(hasBelow && blackNow && !blackBelow){
                upEdges.push_back(make_pair(i,j));
            }
            if(hasBelow && !blackNow && blackBelow){
                belowEdges.push_back(make_pair(i,j));
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
    max = 200;
    detectSourceEdges(max,max);
    int count=0;
    while(!leftEdges.empty() && !rightEdges.empty()){
        count = leftEdges.size() + rightEdges.size();
        detectSourceEdges(max,max);
        vector<pair<int,int>> newLeftEdges = vector<pair<int,int>>();
        vector<pair<int,int>> newRightEdges = vector<pair<int,int>>();
        vector<pair<int,int>> newUpEdges = vector<pair<int,int>>();
        vector<pair<int,int>> newBelowEdges = vector<pair<int,int>>();

        while(!leftEdges.empty()){
            auto p = leftEdges.back();
            leftEdges.pop_back();
            if(!setMaskX(p.first,p.second,true)) newLeftEdges.push_back(p);
        }
        while(!rightEdges.empty()){
            auto p = rightEdges.back();
            rightEdges.pop_back();
            if(!setMaskX(p.first,p.second,false)) newRightEdges.push_back(p);
        }
        */
/*while(!upEdges.empty()){
            auto p = upEdges.back();
            upEdges.pop_back();
            if(!setMaskY(p.first,p.second,true)) newUpEdges.push_back(p);
        }
        while(!belowEdges.empty()){
            auto p = belowEdges.back();
            belowEdges.pop_back();
            if(!setMaskY(p.first,p.second,false)) newBelowEdges.push_back(p);
        }*//*

        leftEdges = newLeftEdges;
        rightEdges = newRightEdges;
        upEdges= newUpEdges;
        belowEdges = newBelowEdges;
        max/=2;
        if(max< 15) break;

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

bool MaskCorrector::setMaskX(int y,int x, bool isLeft){
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
            LOGI("filling left")
            for(int l =0;l<=k;l++){
                result.ptr<unsigned char>(y,x+l)[0]=(isLeft)?0:255;
                result.ptr<unsigned char>(y,x+l)[1]=(isLeft)?0:255;
                result.ptr<unsigned char>(y,x+l)[2]=(isLeft)?0:255;
            }
            return true;
        } else{
            LOGI("filling right")
            for(int l=0;l<=k-1;l++){
                result.ptr<unsigned char>(y,x-l)[0]=(isLeft)?255:0;
                result.ptr<unsigned char>(y,x-l)[1]=(isLeft)?255:0;
                result.ptr<unsigned char>(y,x-l)[2]=(isLeft)?255:0;
            }
            return true;
        }
    }
    return false;
}


bool MaskCorrector::setMaskY(int y,int x, bool isUp){
    for(int k=0;k<CHECK_SIZE;k++){
        int up=INT_MAX;
        int below=INT_MAX;
        if(y+k<source.size().height && !isBlack(edge.ptr<unsigned char>(y+k,x))){
            up = checkNear(y+k,x,1);
        }
        if(y-k>=0 && !isBlack(edge.ptr<unsigned char>(y-k,x))){
            below= checkNear(y-k,x,1);
        }
        if(up==INT_MAX && below==INT_MAX) continue;
        if(up<below)
        {
            LOGI("filling up")
            for(int l =0;l<=k;l++){
                result.ptr<unsigned char>(y+l,x)[0]=(isUp)?0:255;
                result.ptr<unsigned char>(y+l,x)[1]=(isUp)?0:255;
                result.ptr<unsigned char>(y+l,x)[2]=(isUp)?0:255;
            }
            return true;
        } else{
            LOGI("filling below")
            for(int l=0;l<=k-1;l++){
                result.ptr<unsigned char>(y-l,x)[0]=(isUp)?255:0;
                result.ptr<unsigned char>(y-l,x)[1]=(isUp)?255:0;
                result.ptr<unsigned char>(y-l,x)[2]=(isUp)?255:0;
            }
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
*/