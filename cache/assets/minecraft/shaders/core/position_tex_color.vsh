#version 150

// Can't moj_import in things used during startup, when resource packs don't exist.
// This is a copy of dynamicimports.glsl and projection.glsl
layout(std140) uniform DynamicTransforms {
    mat4 ModelViewMat;
    vec4 ColorModulator;
    vec3 ModelOffset;
    mat4 TextureMat;
    float LineWidth;
};
layout(std140) uniform Projection {
    mat4 ProjMat;
};
#moj_import <minecraft:fog.glsl>

in vec3 Position;
in vec2 UV0;
in vec4 Color;

uniform sampler2D Sampler0;

out vec2 texCoord0;
out vec4 vertexColor;

// import functions
#moj_import <expansion:compare_float.glsl>
#moj_import <expansion:shift_texture.glsl>
#moj_import <expansion:dimcheck.glsl>

void main() {
    gl_Position = ProjMat * ModelViewMat * vec4(Position, 1.0);
    vertexColor = Color;

    vec4 corners = texture(Sampler0, vec2(0.0)) * 255.0;

    //checks corner pixel colours
    if (corners == vec4(1.0, 2.0, 3.0, 255.0)) {
        //checks custom biome fog colours
        if (approxEquals(FogColor.rgb * 255.0, vec3(0.0, 4.0, 0.0), 1.0)) {
            texCoord0 = shiftUV(UV0, 4.0, 2.0); //asteroids uv shift
        }
        else if (approxEquals(FogColor.rgb * 255.0, vec3(0.0, 2.0, 0.0), 1.0)) {
            texCoord0 = shiftUV(UV0, 4.0, 1.0); //moon uv shift
        }
        else if (approxEquals(FogColor.rgb * 255.0, vec3(0.0, 0.0, 0.0), 1.0)) {
            texCoord0 = shiftUV(UV0, 4.0, 2.0); //space uv shift
        }
        else if (approxEquals(FogColor.rgb * 255.0, vec3(0.0, 6.0, 0.0), 1.0)) {
            texCoord0 = shiftUV(UV0, 4.0, 3.0); //europa uv shift
        }
        else {
            texCoord0 = vec2(UV0.x, UV0.y / 4.0);
        }
    }
    else texCoord0 = UV0;
}