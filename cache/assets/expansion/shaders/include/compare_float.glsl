bool approxEquals(vec3 v1, vec3 v2, float eps) {
  return all(greaterThan(vec3(eps), abs(v1 - v2)));
}