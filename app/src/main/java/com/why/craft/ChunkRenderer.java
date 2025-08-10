package com.why.craft;

import android.content.Context;
import android.opengl.GLES30;
import android.opengl.Matrix;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;

public class ChunkRenderer {
	private static final int FLOATS_PER_VERTEX = 9; // Position(3) + Normal(3) + Color(3)
	private static final boolean DEBUG_MODE = false; // Set to true to see face normals
	
	private int program;
	private int positionHandle;
	private int normalHandle;
	private int colorHandle;
	private int mvpMatrixHandle;
	private int lightDirHandle;
	private int debugModeHandle; // New for debug mode toggle
	
	// VBO handling
	private int vboId;
	private int vertexCount;
	
	public ChunkRenderer(Context context) {
		createProgram();
		createVBO();
	}
	
	private void createProgram() {
		String vertexShaderCode =
		"uniform mat4 uMVPMatrix;" +
		"attribute vec4 vPosition;" +
		"attribute vec3 vNormal;" +
		"attribute vec3 vColor;" +
		"varying vec3 fColor;" +
		"varying vec3 fNormal;" +
		"void main() {" +
		"  gl_Position = uMVPMatrix * vPosition;" +
		"  fColor = vColor;" +
		"  fNormal = vNormal;" +
		"}";
		
		String fragmentShaderCode =
		"precision mediump float;" +
		"varying vec3 fColor;" +
		"varying vec3 fNormal;" +
		"uniform vec3 uLightDir;" +
		"uniform bool uDebugMode;" + // New uniform
		"void main() {" +
		"  if (uDebugMode) {" +
		"    gl_FragColor = vec4(abs(fNormal), 1.0);" + // Show normals as RGB
		"  } else {" +
		"    float ambient = 0.3;" +
		"    float diff = max(dot(normalize(fNormal), normalize(uLightDir)), 0.0);" +
		"    gl_FragColor = vec4(fColor * (ambient + diff), 1.0);" +
		"  }" +
		"}";
		
		int vertexShader = loadShader(GLES30.GL_VERTEX_SHADER, vertexShaderCode);
		int fragmentShader = loadShader(GLES30.GL_FRAGMENT_SHADER, fragmentShaderCode);
		
		program = GLES30.glCreateProgram();
		GLES30.glAttachShader(program, vertexShader);
		GLES30.glAttachShader(program, fragmentShader);
		GLES30.glLinkProgram(program);
		
		positionHandle = GLES30.glGetAttribLocation(program, "vPosition");
		normalHandle = GLES30.glGetAttribLocation(program, "vNormal");
		colorHandle = GLES30.glGetAttribLocation(program, "vColor");
		mvpMatrixHandle = GLES30.glGetUniformLocation(program, "uMVPMatrix");
		lightDirHandle = GLES30.glGetUniformLocation(program, "uLightDir");
		debugModeHandle = GLES30.glGetUniformLocation(program, "uDebugMode"); // Get handle for debug mode
	}
	
	private int loadShader(int type, String shaderCode) {
		int shader = GLES30.glCreateShader(type);
		GLES30.glShaderSource(shader, shaderCode);
		GLES30.glCompileShader(shader);
		return shader;
	}
	
	private void createVBO() {
		int[] vbos = new int[1];
		GLES30.glGenBuffers(1, vbos, 0);
		vboId = vbos[0];
	}
	
	public void buildMesh(Chunk chunk, World world) {
		if (!chunk.isDirty() || chunk.isEmpty()) return;
		
		List<Float> vertexData = new ArrayList<>();
		
		for (int x = 0; x < chunk.width; x++) {
			for (int y = 0; y < chunk.height; y++) {
				for (int z = 0; z < chunk.depth; z++) {
					if (chunk.getBlock(x, y, z) != Block.AIR) {
						addBlockToMesh(vertexData, x, y, z, chunk, world);
					}
				}
			}
		}
		
		vertexCount = vertexData.size() / FLOATS_PER_VERTEX;
		
		if (vertexCount > 0) {
			FloatBuffer buffer = ByteBuffer.allocateDirect(vertexData.size() * 4)
			.order(ByteOrder.nativeOrder())
			.asFloatBuffer();
			
			for (float value : vertexData) {
				buffer.put(value);
			}
			buffer.position(0);
			
			GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, vboId);
			GLES30.glBufferData(GLES30.GL_ARRAY_BUFFER, buffer.capacity() * 4, buffer, GLES30.GL_STATIC_DRAW);
			GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, 0);
		}
		
		chunk.markClean();
	}
	
	private void addBlockToMesh(List<Float> vertices, int x, int y, int z, Chunk chunk, World world) {
		int blockType = chunk.getBlock(x, y, z);
		if (blockType == Block.AIR) return;
		
		int worldX = chunk.offsetX + x;
		int worldZ = chunk.offsetZ + z;
		float[] color = Block.getColor(blockType);
		
		boolean topVisible = isAirOrOutOfBounds(world, worldX, y+1, worldZ);
		boolean bottomVisible = isAirOrOutOfBounds(world, worldX, y-1, worldZ);
		boolean rightVisible = isAirOrOutOfBounds(world, worldX+1, y, worldZ);
		boolean leftVisible = isAirOrOutOfBounds(world, worldX-1, y, worldZ);
		boolean frontVisible = isAirOrOutOfBounds(world, worldX, y, worldZ+1);
		boolean backVisible = isAirOrOutOfBounds(world, worldX, y, worldZ-1);
		
		if (topVisible) addTopFace(vertices, x, y, z, color);
		if (bottomVisible) addBottomFace(vertices, x, y, z, color);
		if (rightVisible) addRightFace(vertices, x, y, z, color);
		if (leftVisible) addLeftFace(vertices, x, y, z, color);
		if (frontVisible) addFrontFace(vertices, x, y, z, color);
		if (backVisible) addBackFace(vertices, x, y, z, color);
	}
	
	// In ChunkRenderer.java
	// In ChunkRenderer.java
	private boolean isAirOrOutOfBounds(World world, int x, int y, int z) {
		// Use getters for boundaries
		if (x < 0 || x >= world.getWidth() ||
		z < 0 || z >= world.getDepth() ||
		y < 0 || y >= world.getHeight()) {
			return true; // Treat out-of-bounds as air
		}
		return world.getBlock(x, y, z) == Block.AIR;
	}
	
	private void addFace(List<Float> vertices, float x, float y, float z,
	float[] v1, float[] v2, float[] v3, float[] v4,
	float[] normal, float[] color) {
		addVertex(vertices, x+v1[0], y+v1[1], z+v1[2], normal, color);
		addVertex(vertices, x+v2[0], y+v2[1], z+v2[2], normal, color);
		addVertex(vertices, x+v3[0], y+v3[1], z+v3[2], normal, color);
		
		addVertex(vertices, x+v1[0], y+v1[1], z+v1[2], normal, color);
		addVertex(vertices, x+v3[0], y+v3[1], z+v3[2], normal, color);
		addVertex(vertices, x+v4[0], y+v4[1], z+v4[2], normal, color);
	}
	
	private void addVertex(List<Float> vertices, float px, float py, float pz,
	float[] normal, float[] color) {
		vertices.add(px);
		vertices.add(py);
		vertices.add(pz);
		vertices.add(normal[0]);
		vertices.add(normal[1]);
		vertices.add(normal[2]);
		vertices.add(color[0]);
		vertices.add(color[1]);
		vertices.add(color[2]);
	}
	
	private void addTopFace(List<Float> vertices, int x, int y, int z, float[] color) {
		float[] v1 = {0, 1, 0};
		float[] v2 = {1, 1, 0};
		float[] v3 = {1, 1, 1};
		float[] v4 = {0, 1, 1};
		float[] normal = {0, 1, 0};
		addFace(vertices, x, y, z, v1, v2, v3, v4, normal, color);
	}
	
	private void addBottomFace(List<Float> vertices, int x, int y, int z, float[] color) {
		float[] v1 = {0, 0, 0};
		float[] v2 = {1, 0, 0};
		float[] v3 = {1, 0, 1};
		float[] v4 = {0, 0, 1};
		float[] normal = {0, -1, 0};
		addFace(vertices, x, y, z, v1, v2, v3, v4, normal, color);
	}
	
	private void addRightFace(List<Float> vertices, int x, int y, int z, float[] color) {
		float[] v1 = {1, 0, 0};
		float[] v2 = {1, 0, 1};
		float[] v3 = {1, 1, 1};
		float[] v4 = {1, 1, 0};
		float[] normal = {1, 0, 0};
		addFace(vertices, x, y, z, v1, v2, v3, v4, normal, color);
	}
	
	private void addLeftFace(List<Float> vertices, int x, int y, int z, float[] color) {
		float[] v1 = {0, 0, 0};
		float[] v2 = {0, 1, 0};
		float[] v3 = {0, 1, 1};
		float[] v4 = {0, 0, 1};
		float[] normal = {-1, 0, 0};
		addFace(vertices, x, y, z, v1, v2, v3, v4, normal, color);
	}
	
	private void addFrontFace(List<Float> vertices, int x, int y, int z, float[] color) {
		float[] v1 = {0, 0, 1};
		float[] v2 = {1, 0, 1};
		float[] v3 = {1, 1, 1};
		float[] v4 = {0, 1, 1};
		float[] normal = {0, 0, 1};
		addFace(vertices, x, y, z, v1, v2, v3, v4, normal, color);
	}
	
	private void addBackFace(List<Float> vertices, int x, int y, int z, float[] color) {
		float[] v1 = {0, 0, 0};
		float[] v2 = {0, 1, 0};
		float[] v3 = {1, 1, 0};
		float[] v4 = {1, 0, 0};
		float[] normal = {0, 0, -1};
		addFace(vertices, x, y, z, v1, v2, v3, v4, normal, color);
	}
	
	public void renderChunk(float[] vpMatrix, Chunk chunk, World world) {
		if (chunk.isDirty()) {
			buildMesh(chunk, world);
		}
		
		if (chunk.isEmpty() || vertexCount == 0) return;
		
		float[] modelMatrix = new float[16];
		Matrix.setIdentityM(modelMatrix, 0);
		Matrix.translateM(modelMatrix, 0, chunk.offsetX, 0, chunk.offsetZ);
		
		float[] mvpMatrix = new float[16];
		Matrix.multiplyMM(mvpMatrix, 0, vpMatrix, 0, modelMatrix, 0);
		
		GLES30.glUseProgram(program);
		GLES30.glUniformMatrix4fv(mvpMatrixHandle, 1, false, mvpMatrix, 0);
		
		// Light direction (only used if not in debug mode)
		if (DEBUG_MODE) {
			GLES30.glUniform3f(lightDirHandle, 1.0f, 0.0f, 0.0f);
			} else {
			GLES30.glUniform3f(lightDirHandle, 0.5f, 1.0f, 0.5f);
		}
		
		// Pass debug mode toggle to shader
		GLES30.glUniform1i(debugModeHandle, DEBUG_MODE ? 1 : 0);
		
		GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, vboId);
		
		GLES30.glEnableVertexAttribArray(positionHandle);
		GLES30.glVertexAttribPointer(
		positionHandle, 3, GLES30.GL_FLOAT, false,
		FLOATS_PER_VERTEX * 4, 0
		);
		
		GLES30.glEnableVertexAttribArray(normalHandle);
		GLES30.glVertexAttribPointer(
		normalHandle, 3, GLES30.GL_FLOAT, false,
		FLOATS_PER_VERTEX * 4, 3 * 4
		);
		
		GLES30.glEnableVertexAttribArray(colorHandle);
		GLES30.glVertexAttribPointer(
		colorHandle, 3, GLES30.GL_FLOAT, false,
		FLOATS_PER_VERTEX * 4, 6 * 4
		);
		
		GLES30.glDrawArrays(GLES30.GL_TRIANGLES, 0, vertexCount);
		
		GLES30.glDisableVertexAttribArray(positionHandle);
		GLES30.glDisableVertexAttribArray(normalHandle);
		GLES30.glDisableVertexAttribArray(colorHandle);
		GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, 0);
	}
}