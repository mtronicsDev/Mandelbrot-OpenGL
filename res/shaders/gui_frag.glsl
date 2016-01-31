#version 400 core

in vec2 pass_textureCoordinates;

out vec4 out_Color;

uniform sampler2D textureSampler;
uniform vec4 color;

const int MAX_ITERATIONS = 50;
const vec4 C1 = vec4(0, 0, 167.0 / 255.0, 1);
const vec4 C2 = vec4(202.0 / 255.0, 126.0 / 255.0, 1.0 / 255.0, 1);
const vec4 C3 = vec4(1, 1, 1, 1);
const vec4 C4 = vec4(0, 0, 167.0 / 255.0, 1);
const float E = 2.71828182845904523536;

int divergence(float r, float i) {
    float x, y;
    for (int c = 0; c < MAX_ITERATIONS + 20 * log(color.z * E); c++) {
        float x2 = x * x - y * y + r;
        y = 2.0 * x * y + i;
        x = x2;

        if (x * x + y * y > 4.0) return c;
    }

    return int(MAX_ITERATIONS + 20 * log(color.z * E));
}

vec4 blend(float weight) {
    weight = mod(weight, 1.0);
    weight *= 3;
    int floored = int(floor(weight));
    weight -= float(floored);

    vec4 colorA;
    vec4 colorB;

    if (floored == 0) {
      colorA = C1;
      colorB = C2;
    } else if (floored == 1) {
      colorA = C2;
      colorB = C3;
    } else if (floored == 2) {
      colorA = C3;
      colorB = C4;
    } else {
      colorA = C4;
      colorB = C1;
    }

    return mix(colorA, colorB, vec4(weight, weight, weight, 1));
}

vec4 getColor(float r, float i) {
    int divergence = divergence(r, i);
    if (divergence == int(MAX_ITERATIONS + 20 * log(color.z * E))) return vec4(0, 0, 0, 1);

    float mu = float(divergence) + 1.0 - log(max(log(sqrt(r * r + i * i)), .00001)) / log(2.0);

    return blend(mu / 100.0);
}

void main() {
    out_Color = getColor(((pass_textureCoordinates.x - .5) * 2.0 * 1.6) / color.z + color.x,
                        ((pass_textureCoordinates.y + .5) * -2.0 * 0.9) / color.z - color.y);
}