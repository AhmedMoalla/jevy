#version 450 core
layout (location = 0) in vec2 in_position;
layout (location = 1) in vec2 in_texCoords;

out vec2 out_texCoords;

uniform mat4 u_projection;
uniform mat4 u_model;

void main()
{
    out_texCoords = in_texCoords;
    gl_Position = u_projection * u_model * vec4(in_position, 0.0, 1.0);
}