precision mediump float;
varying vec2 v_texCoord;
uniform sampler2D s_texture;

void main() {
    //vec2 flipped_texcoord = vec2(1.0 - v_texCoord.x, 1.0 - v_texCoord.y);
    gl_FragColor = texture2D( s_texture, v_texCoord );
}
