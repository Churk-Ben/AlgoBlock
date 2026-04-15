#version 120
uniform vec2 u_start;
uniform vec2 u_end;
uniform vec2 u_size;
uniform vec4 u_color;
uniform int u_blockStyle;

void main() {
    vec2 p = gl_TexCoord[0].xy;

    vec2 pa = p - u_start;
    vec2 ba = u_end - u_start;
    float h = clamp(dot(pa, ba) / max(dot(ba, ba), 1e-5), 0.0, 1.0);
    vec2 closest = u_start + ba * h;

    vec2 d = abs(p - closest);
    vec2 halfSize = u_size * 0.5;

    if (u_blockStyle == 0) {
        halfSize.x = max(1.0, u_size.x * 0.075);
    }

    vec2 edge = smoothstep(halfSize + 0.5, halfSize - 0.5, d);
    float alpha = edge.x * edge.y;

    // h=0 is u_start (head/target), h=1 is u_end (tail/animated)
    float fade = mix(1.0, 0.15, h);
    if (length(ba) < 1.0) {
        fade = 1.0;
    }

    gl_FragColor = u_color * vec4(1.0, 1.0, 1.0, alpha * fade);
}