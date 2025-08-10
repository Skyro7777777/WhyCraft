package com.why.craft;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class JoystickView extends View {
    private float centerX, centerY;
    private float joystickX, joystickY;
    private float radius;
    private final Paint bgPaint = new Paint();
    private final Paint joystickPaint = new Paint();
    private MovementListener movementListener;

    public interface MovementListener {
        void onMovementChanged(float dx, float dy);
    }

    public JoystickView(Context context, AttributeSet attrs) {
        super(context, attrs);
        bgPaint.setARGB(100, 50, 50, 50);
        joystickPaint.setARGB(200, 100, 100, 100);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        centerX = w / 2f;
        centerY = h / 2f;
        joystickX = centerX;
        joystickY = centerY;
        radius = Math.min(w, h) / 3f;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawCircle(centerX, centerY, radius, bgPaint);
        canvas.drawCircle(joystickX, joystickY, radius / 3, joystickPaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();
        
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_MOVE:
                float dx = x - centerX;
                float dy = y - centerY;
                float distance = (float) Math.sqrt(dx * dx + dy * dy);
                
                if (distance > radius) {
                    dx = dx * radius / distance;
                    dy = dy * radius / distance;
                }
                
                joystickX = centerX + dx;
                joystickY = centerY + dy;
                invalidate();
                
                if (movementListener != null) {
                    movementListener.onMovementChanged(dx / radius, dy / radius);
                }
                break;
                
            case MotionEvent.ACTION_UP:
                joystickX = centerX;
                joystickY = centerY;
                invalidate();
                if (movementListener != null) {
                    movementListener.onMovementChanged(0, 0);
                }
                break;
        }
        return true;
    }

    public void setMovementListener(MovementListener listener) {
        this.movementListener = listener;
    }
}