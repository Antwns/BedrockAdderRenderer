#version 150

precision highp float;

#define ANIMATION_SPEED_MULTIPLIER 1000.
#define WAVE_SCALE_FACTOR 150.

// Function to calculate hue based on a value, returns a color vector. This creates a rainbow-like effect.
// Adds a cosine-modulated variation to RGB channels to generate color changes over time.
#define hue(v)  ((.6 + .6 * cos(6.*(v) + vec4(0, 23, 21, 1))) + vec4(0., 0., 0., 1.) /*set alpha to 1*/)

#define finalize() { \
    vertexDistance = length((ModelViewMat * vertex).xyz); \
    texCoord0 = UV0; \
}

in vec3 Position;
in vec4 Color;
in vec2 UV0;
in ivec2 UV2;

// Sampler0 is the current texture, which seems to contain more than one texture. It may have some optimization handled by OpenGL.
uniform sampler2D Sampler0;
// Sampler2 seems to always be a 16x16 texture, as indicated by the textureSize() function.
uniform sampler2D Sampler2;

uniform mat4 ModelViewMat;
uniform mat4 ProjMat;
// Global time of the game world, represented in fractional days. It is derived from the Minecraft game time value.
uniform float GameTime;

out float vertexDistance;
out vec4 vertexColor;
out vec2 texCoord0;

float safeGameTime() {
    float gameTime = GameTime;
    // Fallback for AMD GPUs bug.
    if(gameTime <= 0) {
        gameTime = 0.5;
    }
    return gameTime;
}

float scaledTime() {
    return safeGameTime() * 12000.;
}

void calculate_gl_pos(inout vec4 vertex) {
    gl_Position = ProjMat * ModelViewMat * vertex;
}

void default_color() {
    vertexColor = Color * texelFetch(Sampler2, UV2 / 16, 0);
}

//<SCOREBOARD_HIDE_BACKGROUND>
void apply_scoreboard_hide_background(inout vec4 vertex) {
    calculate_gl_pos(vertex);
    if (Position.z == 0. && gl_Position.x > 0.95) {
        vertexColor = vec4(0);
    } else {
        default_color();
    }
    finalize();
}
//</SCOREBOARD_HIDE_BACKGROUND>

//<TEXT_EFFECTS>
void rainbow_effect() {
    vertexColor = hue(gl_Position.x + safeGameTime() * ANIMATION_SPEED_MULTIPLIER) * texelFetch(Sampler2, UV2 / 16, 0);
}

void wavy_effect_1() {
    gl_Position.y += sin(scaledTime() + (gl_Position.x * 6)) / WAVE_SCALE_FACTOR;
}

void apply_rainbow_effect(inout vec4 vertex) {
    calculate_gl_pos(vertex);
    rainbow_effect();
    finalize();
}

void apply_wavy_effect(inout vec4 vertex) {
    calculate_gl_pos(vertex);
    default_color();
    wavy_effect_1();
    finalize();
}

void apply_rainbow_wavy_effect(inout vec4 vertex) {
    calculate_gl_pos(vertex);
    wavy_effect_1();
    rainbow_effect();
    finalize();
}

void apply_jump_animation(inout vec4 vertex) {
    default_color();
    float vertexId = mod(gl_VertexID, 4.0);
    if (vertex.z <= 0.) {
        if (vertexId == 3. || vertexId == 0.) {
            vertex.y += cos(scaledTime() / 4) * 0.1;
            vertex.y += max(cos(scaledTime() / 4) * 0.1, 0.);
        }
    } else {
        if (vertexId == 3. || vertexId == 0.) {
            vertex.y -= cos(scaledTime() / 4) * 3;
            vertex.y -= max(cos(scaledTime() / 4) * 4, 0.);
        }
    }
    calculate_gl_pos(vertex);
    finalize();
}

void apply_rainbow_jump_animation(inout vec4 vertex) {
    float vertexId = mod(gl_VertexID, 4.0);
    if (vertex.z <= 0.) {
        if (vertexId == 3. || vertexId == 0.) {
            vertex.y += cos(scaledTime() / 4) * 0.1;
            vertex.y += max(cos(scaledTime() / 4) * 0.1, 0.);
        }
    } else {
        if (vertexId == 3. || vertexId == 0.) {
            vertex.y -= cos(scaledTime() / 4) * 3;
            vertex.y -= max(cos(scaledTime() / 4) * 4, 0.);
        }
    }
    rainbow_effect();
    calculate_gl_pos(vertex);
    finalize();
}

void apply_blinking_effect(inout vec4 vertex, float speed) {
    calculate_gl_pos(vertex);
    float blink = abs(sin(scaledTime() * speed));
    vertexColor = Color * blink * texelFetch(Sampler2, UV2 / 16, 0);
    finalize();
}
//</TEXT_EFFECTS>

//<NOSHADOW>
void apply_remove_shadow(inout vec4 vertex) {
    calculate_gl_pos(vertex);
    default_color();
    vertexColor = vec4(1, 1, 1, vertexColor.a); // White color
    finalize();
}
//</NOSHADOW>

void main() {
    vec4 vertex = vec4(Position, 1.0);
    ivec3 iColor = ivec3(Color.xyz * 255 + vec3(0.5));

    // Hide scoreboard background.
    //<SCOREBOARD_HIDE_BACKGROUND>
    if (iColor == ivec3(255, 85, 85))
    {
        apply_scoreboard_hide_background(vertex);
        return;
    }
    //</SCOREBOARD_HIDE_BACKGROUND>

    // Apply custom effects also to shadows.
    if (fract(Position.z) < 0.01) {
        //<NOSHADOW>
        // Shadow of #4e5c24
        if (iColor == ivec3(19, 23, 9))
        {
            gl_Position = vec4(2, 2, 2, 1);
            default_color();
            finalize();
            return;
        }
        //</NOSHADOW>

        //<TEXT_EFFECTS>
        // Shadow color: #393f3f, Original text color: #e6fffe - Apply rainbow effect.
        if (iColor == ivec3(57, 63, 63)) {
            // Nothing to do here, don't apply rainbow effect to shadow.
            // Default rendering for vanilla text.
            calculate_gl_pos(vertex);
            default_color();
            finalize();
            return;
        }

        // Shadow color: #393f3e, Original text color: #e6fffa - Apply wavy effect.
        if (iColor == ivec3(57, 63, 62)) {
            apply_wavy_effect(vertex);
            return;
        }

        // Shadow color: #393e3f, Original text color: #e6fbfe - Apply rainbow + wavy effect.
        if (iColor == ivec3(57, 62, 63)) {
            // Don't apply rainbow effect to shadow, only apply the wavy effect.
            apply_wavy_effect(vertex);
            return;
        }

        // Shadow color: #393e3e, Original text color: #e6fbfa - Apply jump animation.
        if (iColor == ivec3(57, 62, 62)) {
            apply_jump_animation(vertex);
            return;
        }

        // Shadow color: #393d3f, Original text color: #e6f7fe - Apply rainbow + jump animation.
        if (iColor == ivec3(57, 61, 63)) {
            apply_jump_animation(vertex);// Don't apply rainbow effect to shadow.
            return;
        }

        // Shadow color: #393d3e, Original text color: #e6f7fa - Apply blinking effect.
        if (iColor == ivec3(57, 61, 62)) {
            apply_blinking_effect(vertex, .5);
            return;
        }

        //%USER_TEXT_EFFECTS_SHADOWS% // TODO

        //</TEXT_EFFECTS>
    }

    //<NOSHADOW>
    // #4e5c24 - Remove shadow from text
    if (iColor == ivec3(78, 92, 36))
    {
        apply_remove_shadow(vertex);
        return;
    }
    //</NOSHADOW>

    //<TEXT_EFFECTS>
    // Apply custom effects to texts.
    // #e6fffe - Apply rainbow effect.
    if (iColor == ivec3(230, 255, 254))
    {
        apply_rainbow_effect(vertex);
        return;
    }

    // #e6fffa - Apply wavy effect.
    if (iColor == ivec3(230, 255, 250))
    {
        apply_wavy_effect(vertex);
        return;
    }

    // #e6fbfe - Apply rainbow + wavy effect.
    if (iColor == ivec3(230, 251, 254))
    {
        apply_rainbow_wavy_effect(vertex);
        return;
    }

    // #e6fbfa - Apply jump animation.
    if (iColor == ivec3(230, 251, 250))
    {
        apply_jump_animation(vertex);
        return;
    }

    // #e6f7fe - Apply rainbow + jump animation.
    if (iColor == ivec3(230, 247, 254))
    {
        apply_rainbow_jump_animation(vertex);
        return;
    }

    // #e6f7fa - Apply blinking effect.
    if (iColor == ivec3(230, 247, 250))
    {
        apply_blinking_effect(vertex, .5);
        return;
    }

    //%USER_TEXT_EFFECTS% // TODO
    //</TEXT_EFFECTS>

    //<LEGACY_TEXT_EFFECTS>
    // Legacy ItemsAdder text effects colors support.
    // #fffffe - Apply rainbow effect.
    if (iColor == ivec3(255, 255, 254))
    {
        apply_rainbow_effect(vertex);
        return;
    }

    // #fffffd - Apply wavy effect.
    if (iColor == ivec3(255, 255, 253))
    {
        apply_wavy_effect(vertex);
        return;
    }

    // #fffffc - Apply rainbow + wavy effect.
    if (iColor == ivec3(255, 255, 25))
    {
        apply_rainbow_wavy_effect(vertex);
        return;
    }

    // #fffffb - Apply jump animation.
    if (iColor == ivec3(255, 255, 251))
    {
        apply_jump_animation(vertex);
        return;
    }

    // #fffefe - Apply rainbow + jump animation.
    if (iColor == ivec3(255, 254, 254))
    {
        apply_rainbow_jump_animation(vertex);
        return;
    }
    //</LEGACY_TEXT_EFFECTS>

    // Default rendering for vanilla text.
    calculate_gl_pos(vertex);
    default_color();
    finalize();
}