#version 150

#moj_import <minecraft:fog.glsl>
#moj_import <expansion:compare_float.glsl>
#moj_import <expansion:sun_radius.glsl>
#moj_import <expansion:dimcheck.glsl>
#moj_import <expansion:hash.glsl>
#moj_import <expansion:noise.glsl>
#moj_import <minecraft:dynamictransforms.glsl>
#moj_import <minecraft:globals.glsl>

in mat4 ProjInv;
in float isSky;
in float sphericalVertexDistance;
in float cylindricalVertexDistance;

out vec4 fragColor;

void main() {
    if (FromExpansion(FogColor)) {
        #moj_import <expansion:sky.glsl>
    }
    else {
        fragColor = apply_fog(ColorModulator, sphericalVertexDistance, cylindricalVertexDistance, 0.0, FogSkyEnd, FogSkyEnd, FogSkyEnd, FogColor);
    }
}
