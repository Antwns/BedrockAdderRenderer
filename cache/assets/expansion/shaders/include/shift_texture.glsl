vec2 shiftUV(vec2 UV, float frames, float shift) 
{
  return vec2(UV.x, (UV.y / frames) + (shift / frames));
}