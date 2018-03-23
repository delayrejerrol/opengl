//
// Created by jerro on 3/8/2018.
//

#ifndef COINFALLINGCPP_COIN_H
#define COINFALLINGCPP_COIN_H

#include <GLES2/gl2.h>

class Coin {

public:
    Coin(int currentCoinFace, float pointX, float pointY);

    int getCurrentCoinFace();
    int getNextCoinFace();
    GLuint getTextureId();
    float getY();
    void Render(GLuint textureId, GLuint positionHandle, GLuint texCoord, GLint matrixHandle, GLint samplerLoc);
    void setTextureId(GLuint textureId);
    void translate(float deltaX, float deltaY);

private:
    int mCurrentCoinFace;
    GLuint mTextureId;
    void updateTransformedVertices();
};
#endif //COINFALLINGCPP_COIN_H
