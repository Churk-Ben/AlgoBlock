#version 120
uniform vec2 u_start;
uniform vec2 u_end;
uniform vec2 u_size;
uniform vec4 u_color;
uniform int u_blockStyle;

void main() {
    vec2 p = gl_TexCoord[0].xy;

    vec2 halfSize = u_size * 0.5;
    if (u_blockStyle == 0) {
        halfSize.x = max(1.0, u_size.x * 0.075);
    }

    // 1. Base cursor at u_start (head/target position)
    vec2 dBase = abs(p - u_start);
    vec2 edgeBase = smoothstep(halfSize + 0.5, halfSize - 0.5, dBase);
    float alphaBase = edgeBase.x * edgeBase.y;

    // 2. Trail polygon from u_start to u_end
    vec2 pa = p - u_start;
    vec2 ba = u_end - u_start;
    
    float alphaTrail = 0.0;
    if (length(ba) > 0.001) {
        float h = clamp(dot(pa, ba) / dot(ba, ba), 0.0, 1.0);
        vec2 closest = u_start + ba * h;
        vec2 dTrail = abs(p - closest);
        vec2 edgeTrail = smoothstep(halfSize + 0.5, halfSize - 0.5, dTrail);
        
        // Trail fades out from head (h=0) to tail (h=1)
        float fade = mix(1.0, 0.15, h);
        alphaTrail = edgeTrail.x * edgeTrail.y * fade;
    }

    // Combine base and trail (Max blending to prevent deformation)
    float finalAlpha = max(alphaBase, alphaTrail);

    gl_FragColor = u_color * vec4(1.0, 1.0, 1.0, finalAlpha);
}
