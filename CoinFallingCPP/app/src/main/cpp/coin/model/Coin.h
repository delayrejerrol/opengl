//
// Created by jerro on 3/8/2018.
//

#ifndef COINFALLINGCPP_COIN_H
#define COINFALLINGCPP_COIN_H

#include <GLES2/gl2.h>
#include <coin/graphics/Matrix.h>

class Coin {

public:
    Coin(GLuint currentCoinFace, float pointX, float pointY, float scaleValue);
    ~Coin();
    GLuint getCurrentTextureId();
    GLuint getTextureId();
    float getY();
    void Render(GLuint textureId, Matrix *mMVPMatrix, GLuint positionHandle, GLuint texCoord, GLint matrixHandle, GLint samplerLoc);
    void setTextureId(GLuint textureId);
    void setTextures(GLuint textures[]);
    void translate(float deltaX, float deltaY);

private:
    // Vertices
    GLfloat mVertices[12];
    // Rect
    GLfloat mBase[4];
    // Point
    GLfloat mTranslation[2];

    int mCurrentCoinFace;

    GLuint *mTextures;
    GLuint mTextureId;

    void updateTransformedVertices();
};
#endif //COINFALLINGCPP_COIN_H
