bool FromExpansion(vec4 FogColor) 
{
  // Expansion dimensions all have a very slight green tint that shouldn't occur in vanilla
  // except for space which is all black
  return ((FogColor.g > FogColor.r && FogColor.g > FogColor.b) || FogColor.rgb == vec3(0.0, 0.0, 0.0));
}