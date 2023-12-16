#version 450 core
in vec2 out_texCoords;

out vec4 color;

uniform sampler2D u_texture;
uniform vec3 u_spriteColor;

void main()
{
    color = vec4(u_spriteColor, 1.0) * texture(u_texture, out_texCoords);
}