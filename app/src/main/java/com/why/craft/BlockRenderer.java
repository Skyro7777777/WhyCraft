package com.why.craft;

import android.content.Context;
import android.opengl.GLES30;
import android.opengl.Matrix;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

public class BlockRenderer {
    private final FloatBuffer vertexBuffer;
    private int program;
    private int positionHandle;
    private int colorHandle;
    private int mvpMatrixHandle;
    
    private final float[] cubeVertices = {
        // Front face
        -0.5f, -0.5f, 0.5f,
        0.5f, -0.5f, 0.5f,
        0.5f, 0.5f, 0.5f,
        -0.5f, 0.5f, 0.5f,
        
        // Back face
        -0.5f, -0.5f, -0.5f,
        -0.5f, 0.5f, -0.5f,
        0.5f, 0.5f, -0.5f,
        0.5f, -0.5f, -0.5f,
        
        // Top face
        -0.5f, 0.5f, -0.5f,
        -0.5f, 0.5f, 0.5f,
        0.5f, 0.5f, 0.5f,
        0.5f, 0.5f, -0.5f,
        
        // Bottom face
        -0.5f, -0.5f, -0.5f,
        0.5f, -0.5f, -0.5f,
        0.5f, -0.5f, 0.5f,
        -0.5f, -0.5f, 0.5f,
        
        // Right face
        0.5f, -0.5f, -0.5f,
        0.5f, 0.5f, -0.5f,
        0.5f, 0.5f, 0.5f,
        0.5f, -0.5f, 0.5f,
        
        // Left face
        -0.5f, -0.5f, -0.5f,
        -0.5f, -0.5f, 0.5f,
        -0.5f, 0.5f, 0.5f,
        -0.5f, 0.5f, -0.5f
    };

    public BlockRenderer(Context context) {
        ByteBuffer bb = ByteBuffer.allocateDirect(cubeVertices.length * 4);
        bb.order(ByteOrder.nativeOrder());
        vertexBuffer = bb.asFloatBuffer();
        vertexBuffer.put(cubeVertices);
        vertexBuffer.position(0);
        
        createProgram();
    }

    private void createProgram() {
		String vertexShaderCode =
		"uniform mat4 uMVPMatrix;" +
		"attribute vec4 vPosition;" +
		"void main() {" +
		"  gl_Position = uMVPMatrix * vPosition;" +
		"}";
		
		String fragmentShaderCode =
		"precision mediump float;" +
		"uniform vec4 vColor;" +
		"void main() {" +
		"  gl_FragColor = vColor;" +
		"}";
		
		int vertexShader = loadShader(GLES30.GL_VERTEX_SHADER, vertexShaderCode);
		int fragmentShader = loadShader(GLES30.GL_FRAGMENT_SHADER, fragmentShaderCode);
		
		program = GLES30.glCreateProgram();
		GLES30.glAttachShader(program, vertexShader);
		GLES30.glAttachShader(program, fragmentShader);
		GLES30.glLinkProgram(program);  // Added program parameter
		
		positionHandle = GLES30.glGetAttribLocation(program, "vPosition");
		colorHandle = GLES30.glGetUniformLocation(program, "vColor");
		mvpMatrixHandle = GLES30.glGetUniformLocation(program, "uMVPMatrix");
	}

    private int loadShader(int type, String shaderCode) {
        int shader = GLES30.glCreateShader(type);
        GLES30.glShaderSource(shader, shaderCode);
        GLES30.glCompileShader(shader);
        return shader;
    }

    public void renderBlock(float[] vpMatrix, float x, float y, float z, int blockType) {
        float[] modelMatrix = new float[16];
        Matrix.setIdentityM(modelMatrix, 0);
        Matrix.translateM(modelMatrix, 0, x, y, z);
        
        float[] mvpMatrix = new float[16];
        Matrix.multiplyMM(mvpMatrix, 0, vpMatrix, 0, modelMatrix, 0);
        
        GLES30.glUseProgram(program);
        GLES30.glUniformMatrix4fv(mvpMatrixHandle, 1, false, mvpMatrix, 0);
        
        float[] color = Block.getColor(blockType);
        GLES30.glUniform4fv(colorHandle, 1, color, 0);
        
        GLES30.glEnableVertexAttribArray(positionHandle);
        GLES30.glVertexAttribPointer(positionHandle, 3, GLES30.GL_FLOAT, false, 0, vertexBuffer);
        
        for (int i = 0; i < 6; i++) {
            GLES30.glDrawArrays(GLES30.GL_TRIANGLE_FAN, i * 4, 4);
        }
        
        GLES30.glDisableVertexAttribArray(positionHandle);
    }
}