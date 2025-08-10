package com.why.craft;

import android.content.Context;
import android.opengl.GLES30;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class GameRenderer implements GLSurfaceView.Renderer {
    private final Context context;
    private final float[] projectionMatrix = new float[16];
    private final float[] viewMatrix = new float[16];
    private final float[] vpMatrix = new float[16];
    private World world;
    private Player player;
    private ChunkRenderer chunkRenderer;

    public GameRenderer(Context context) {
        this.context = context;
        this.world = new World(128, 64, 128);
        this.player = new Player(64, 20, 64);
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        GLES30.glClearColor(0.5f, 0.7f, 1.0f, 1.0f);
        GLES30.glEnable(GLES30.GL_DEPTH_TEST);
        GLES30.glEnable(GLES30.GL_CULL_FACE);
        chunkRenderer = new ChunkRenderer(context);
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        GLES30.glViewport(0, 0, width, height);
        float ratio = (float) width / height;
        Matrix.perspectiveM(projectionMatrix, 0, 90, ratio, 0.1f, 300f);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT | GLES30.GL_DEPTH_BUFFER_BIT);
        
        player.update(world);
        
        Matrix.setLookAtM(viewMatrix, 0, 
            player.x, player.y, player.z,
            player.x + player.lookX, player.y + player.lookY, player.z + player.lookZ,
            0, 1, 0);
        Matrix.multiplyMM(vpMatrix, 0, projectionMatrix, 0, viewMatrix, 0);
        
        world.render(vpMatrix, chunkRenderer);
    }
    
    // ADD THESE GETTER METHODS
    public Player getPlayer() {
        return player;
    }
    
    public World getWorld() {
        return world;
    }
}