float SunRadius(vec4 fogColor) {
    float SunRad;
    if (approxEquals(fogColor.rgb * 255.0, vec3(0.0, 4.0, 0.0), 1.0)){
        SunRad = 0.02; // sun size in asteroids
    }
    else if (approxEquals(fogColor.rgb * 255.0, vec3(0.0, 2.0, 0.0), 1.0)){
        SunRad = 0.08; //sun size on the moon
    }
    else if (approxEquals(fogColor.rgb * 255.0, vec3(0.0, 0.0, 0.0), 1.0)){
        SunRad = 0.1; //sun size in space
    }
    else if (approxEquals(fogColor.rgb * 255.0, vec3(0.0, 6.0, 0.0), 1.0)){
        SunRad = 0.03; //sun size on europa 
    }
    return SunRad;
}