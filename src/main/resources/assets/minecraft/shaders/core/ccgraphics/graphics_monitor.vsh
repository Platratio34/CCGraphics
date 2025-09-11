#version 150

#moj_import <fog.glsl>

in vec3 Position;
in vec2 UV0;

uniform mat4 ModelViewMat;
uniform mat4 ProjMat;
uniform vec3 ChunkOffset;
uniform int FogShape;

uniform sampler2D MonitorFrame;
uniform vec4 ColorModulator;

// out float vertexDistance;
out vec2 texUv;

void main() {
    vec3 pos = Position + ChunkOffset;
    gl_Position = ProjMat * ModelViewMat * vec4(pos, 1.0);

    // vertexDistance = fog_distance(pos, FogShape);
    texUv = UV0;
}