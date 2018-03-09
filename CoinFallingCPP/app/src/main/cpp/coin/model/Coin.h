//
// Created by jerro on 3/8/2018.
//

#ifndef COINFALLINGCPP_COIN_H
#define COINFALLINGCPP_COIN_H
class Coin {

public:
    const float *vertices[];
    const float *indices[];
    const float *uvCoord[];

    Coin(int *currentCoinFace, float *pointX, float *pointY);

    int getCurrentCoinFace();
    int getNextCoinFace(int currentCoinFace);
    int getTextureId();
    float getTransformVertices()[12]; // Return 4 side of vertices
    float getY();
    void Render();
    void setTextureId(int textureId);
    void translate(float deltaX, float deltaY);

private:
    const int *currentCoinFace;
    const int *textureId;
    const float *base[];
    const float *translation[];
};
#endif //COINFALLINGCPP_COIN_H
