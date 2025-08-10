package com.why.craft;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;

public class GameView extends GLSurfaceView {
    private final GameRenderer renderer;

    public GameView(Context context) {
        super(context);
        renderer = new GameRenderer(context);  // Removed assignment to final variable
        init(context);
    }

    public GameView(Context context, AttributeSet attrs) {
        super(context, attrs);
        renderer = new GameRenderer(context);  // Removed assignment to final variable
        init(context);
    }

    private void init(Context context) {
        setEGLContextClientVersion(3);
        setRenderer(renderer);
        setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
    }

    public GameRenderer getRenderer() {
        return renderer;
    }
}