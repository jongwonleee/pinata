//
// Created by 이종원 on 2020/05/14.
//

#ifndef AI_IMAGE_EDITOR_SIZE_CHANGER_H
#define AI_IMAGE_EDITOR_SIZE_CHANGER_H


#include <opencv2/core.hpp>
#include <opencv2/imgproc.hpp>
#include <vector>

using namespace cv;
using namespace std;

class SizeChanger{
private:
    Mat source,dSource;
    Mat mask, dMask;
    int sampleRate=1;
    Mat upsampling(Mat result);
    void downsampling();
public:
    SizeChanger(Mat source, Mat mask);
    void getDownSized(Mat* source, Mat* mask);
    Mat getMerged(Mat result);
};
#endif //AI_IMAGE_EDITOR_SIZE_CHANGER_H
