//
// Created by jerro on 3/8/2018.
//

#ifndef COINFALLINGCPP_COIN_H
#define COINFALLINGCPP_COIN_H

#include <GLES2/gl2.h>
#include <coin/graphics/Matrix.h>

class Coin {

public:
    Coin(GLuint currentCoinFace, float pointX, float pointY);
    ~Coin();
    int getCurrentCoinFace();
    int getNextCoinFace();
    GLuint getCurrentTextureId();
    GLuint getTextureId();
    float getY();
    void Render(GLuint textureId, Matrix *mMVPMatrix, GLuint positionHandle, GLuint texCoord, GLint matrixHandle, GLint samplerLoc);
    void setTextureId(GLuint textureId);
    void setTextures(GLuint textures[]);
    void translate(float deltaX, float deltaY);

private:
    GLfloat mVertices[12];

    // Rect
    //float mBase[] = { -50.0f, 50.0f, 50.0f, -50.0f };
    GLfloat mBase[4];
    // Point
    GLfloat mTranslation[2];

    // The order of vertex rendering for a quad
    /*GLshort mIndices[6] = {
            0, 1, 2,
            0, 2, 3
    };*/

    /*GLfloat mUVCoord[8] = {
            0.0f, 0.0f,
            0.0f, 1.0f,
            1.0f, 1.0f,
            1.0f, 0.0f
    };*/

    int mCurrentCoinFace;

    GLuint *mTextures;
    GLuint mTextureId;

    void updateTransformedVertices();
};
#endif //COINFALLINGCPP_COIN_H
