package com.why.craft;

import android.content.Context;
import android.opengl.GLES30;
import android.opengl.Matrix;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

public class Skybox {
    private final FloatBuffer vertexBuffer;
    private int program;
    private int positionHandle;
    private int mvpMatrixHandle;
    
    private final float[] skyboxVertices = {
        -1,  1, -1,
        -1, -1, -1,
         1, -1, -1,
         1,  1, -1,
        
        -1, -1,  1,
        -1, -1, -1,
        -1,  1, -1,
        -1,  1,  1,
        
         1, -1, -1,
         1, -1,  1,
         1,  1,  1,
         1,  1, -1,
        
        -1, -1,  1,
        -1,  1,  1,
         1,  1,  1,
         1, -1,  1,
        
        -1,  1, -1,
         1,  1, -1,
         1,  1,  1,
        -1,  1,  1,
        
        -1, -1, -1,
        -1, -1,  1,
         1, -1,  1,
         1, -1, -1
    };

    public Skybox(Context context) {
        ByteBuffer bb = ByteBuffer.allocateDirect(skyboxVertices.length * 4);
        bb.order(ByteOrder.nativeOrder());
        vertexBuffer = bb.asFloatBuffer();
        vertexBuffer.put(skyboxVertices);
        vertexBuffer.position(0);
        
        createProgram();
    }

    private void createProgram() {
        String vertexShaderCode =
            "uniform mat4 uMVPMatrix;" +
            "attribute vec3 vPosition;" +
            "void main() {" +
            "  gl_Position = uMVPMatrix * vec4(vPosition, 1.0);" +
            "}";
            
        String fragmentShaderCode =
            "precision mediump float;" +
            "void main() {" +
            "  gl_FragColor = vec4(0.5, 0.7, 1.0, 1.0);" +  // Sky blue
            "}";
            
        int vertexShader = loadShader(GLES30.GL_VERTEX_SHADER, vertexShaderCode);
        int fragmentShader = loadShader(GLES30.GL_FRAGMENT_SHADER, fragmentShaderCode);
        
        program = GLES30.glCreateProgram();
        GLES30.glAttachShader(program, vertexShader);
        GLES30.glAttachShader(program, fragmentShader);
        GLES30.glLinkProgram(program);
        
        positionHandle = GLES30.glGetAttribLocation(program, "vPosition");
        mvpMatrixHandle = GLES30.glGetUniformLocation(program, "uMVPMatrix");
    }

    private int loadShader(int type, String shaderCode) {
        int shader = GLES30.glCreateShader(type);
        GLES30.glShaderSource(shader, shaderCode);
        GLES30.glCompileShader(shader);
        return shader;
    }

    public void render(float[] vpMatrix, Player player) {
        // Remove translation, only rotation matters for skybox
        float[] modelMatrix = new float[16];
        Matrix.setIdentityM(modelMatrix, 0);
        Matrix.translateM(modelMatrix, 0, -player.x, -player.y, -player.z);
        
        float[] mvpMatrix = new float[16];
        Matrix.multiplyMM(mvpMatrix, 0, vpMatrix, 0, modelMatrix, 0);
        
        GLES30.glUseProgram(program);
        GLES30.glUniformMatrix4fv(mvpMatrixHandle, 1, false, mvpMatrix, 0);
        
        GLES30.glEnableVertexAttribArray(positionHandle);
        GLES30.glVertexAttribPointer(positionHandle, 3, GLES30.GL_FLOAT, false, 0, vertexBuffer);
        
        // Draw skybox
        GLES30.glDrawArrays(GLES30.GL_TRIANGLE_STRIP, 0, 4);
        GLES30.glDrawArrays(GLES30.GL_TRIANGLE_STRIP, 4, 4);
        GLES30.glDrawArrays(GLES30.GL_TRIANGLE_STRIP, 8, 4);
        GLES30.glDrawArrays(GLES30.GL_TRIANGLE_STRIP, 12, 4);
        GLES30.glDrawArrays(GLES30.GL_TRIANGLE_STRIP, 16, 4);
        GLES30.glDrawArrays(GLES30.GL_TRIANGLE_STRIP, 20, 4);
        
        GLES30.glDisableVertexAttribArray(positionHandle);
    }
}