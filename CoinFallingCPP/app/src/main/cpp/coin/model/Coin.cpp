//
// Created by jerro on 3/8/2018.
//

#include "Coin.h"

Coin::Coin(int *currentCoinFace, float *pointX, float *pointY) {
    this->currentCoinFace = currentCoinFace;

    this->base = {-50f, 50f, 50f, -50f};

    this->translation = {pointX, pointY};

    vertices = getTransformVertices();

    // The order of vertex rendering for a quad
    indices = {
            0, 1, 2,
            0, 2, 3
    };

    // Create our UV coordinates
    uvCoord = {
            0.0f, 0.0f,
            0.0f, 0.1f,
            1.0f, 1.0f,
            1.0f, 0.0f
    };
}

int Coin::getCurrentCoinFace() {
    return this->currentCoinFace;
}

int Coin::getNextCoinFace(int currentCoinFace) {
    int nextCoinFace = currentCoinFace;
    if (nextCoinFace == 7)
        nextCoinFace = 0;
    else
        nextCoinFace++;

    this->currentCoinFace = nextCoinFace;
    return nextCoinFace;
}

int Coin::getTextureId() {
    return this->textureId;
}

float Coin::getTransformVertices()[12] {
    const float *left   = this->base[0]; // x1
    const float *right  = this->base[1]; // x2
    const float *bottom = this->base[2]; // y1
    const float *top    = this->base[3]; // y2

    // We now detach from our Rect because when rotating,
    // we need the seperate points, so we do so in opengl order
    const float *one[] = { left, top };
    const float *two[] = { left, bottom };
    const float *three[] = { right, bottom };
    const float *four[] = { right, top };

    // Finally we translate the coin to its correct position.
    one[0] = this->translation[0];
    one[1] = this->translation[1];

    two[0] = this->translation[0];
    two[1] = this->translation[1];

    three[0] = this->translation[0];
    three[1] = this->translation[1];

    four[0] = this->translation[0];
    four[1] = this->translation[1];

    // We now return our float array of vertices.
    return {
            one[0], one[1], 0.0f,
            two[0], two[1], 0.0f,
            three[0], three[1], 0.0f,
            four[0], four[1], 0.0f
    };
}

float Coin::getY() {
    return this->translation[1];
}

void Coin::Render() {

}

void Coin::setTextureId(int textureId) {
    this->textureId = textureId;
}

void Coin::translate(float deltaX, float deltaY) {
    this->translation[0] = deltaX;
    this->translation[1] = deltaY;

    vertices = getTransformVertices();
}

// Render

