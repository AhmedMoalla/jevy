#version 410 core
#ifdef GL_ES
precision mediump float;
#endif
in vec4 v_col;
out vec4 FragColor;
void main() {
    FragColor = v_col;
}