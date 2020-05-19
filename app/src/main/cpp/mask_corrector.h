//
// Created by 이종원 on 2020/05/02.
//

#ifndef AI_IMAGE_EDITOR_MASK_CORRECTOR_H
#define AI_IMAGE_EDITOR_MASK_CORRECTOR_H

#include <opencv2/core.hpp>
#include <opencv2/imgproc.hpp>
#include <vector>

using namespace cv;
using namespace std;


class MaskCorrector{
private:
    Mat source, mask, result;
    Mat edge;
    vector<pair<int,int>> leftEdges;
    vector<pair<int,int>> rightEdges;
    int CHECK_SIZE = 3;
    void Correct();
    bool isBlack(uchar * ptr);
    bool setMask(int, int, bool);
    bool setMaskByNear(int, int, bool);
    int checkNear(int,int,int);
    void detectMaskEdges();
    void detectSharpMaskEdges();
    void detectSourceEdges(int,int);
    void color(int x,int y);
public:
    MaskCorrector(Mat source ,Mat mask);
    Mat getResult();
    void Run();
};


#endif AI_IMAGE_EDITOR_MASK_CORRECTOR_H
