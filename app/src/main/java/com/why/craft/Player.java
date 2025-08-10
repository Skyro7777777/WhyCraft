package com.why.craft;

public class Player {
    public float x, y, z;
    public float lookX, lookY, lookZ;
    public float velX, velY, velZ;
    public boolean onGround;
    public boolean moveForward, moveBackward, moveLeft, moveRight, jump;

    private static final float GRAVITY = -0.03f;
    private static final float MOVE_SPEED = 0.15f;
    private static final float JUMP_FORCE = 0.25f;

    public Player(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
        updateLookVector();
    }

    private void updateLookVector() {
        // Simple forward direction (can be replaced with yaw/pitch later)
        lookX = 0;
        lookY = 0;
        lookZ = 1;
    }
	// In Player.java
	
	public boolean isColliding(World world, float px, float py, float pz) {
    // Player bounding box (width=0.6f, height=1.8f)
    for (int ix = (int)(px - 0.3f); ix <= (int)(px + 0.3f); ix++) {
        for (int iy = (int)py; iy <= (int)(py + 1.8f); iy++) {
            for (int iz = (int)(pz - 0.3f); iz <= (int)(pz + 0.3f); iz++) {
                if (world.getBlock(ix, iy, iz) != Block.AIR) {
                    return true;
                }
            }
        }
    }
    return false;
	}

	
    public void update(World world) {
        // Gravity
        velY += GRAVITY;

        float moveX = 0, moveZ = 0;
        if (moveForward) {
            moveX += lookX * MOVE_SPEED;
            moveZ += lookZ * MOVE_SPEED;
        }
        if (moveBackward) {
            moveX -= lookX * MOVE_SPEED;
            moveZ -= lookZ * MOVE_SPEED;
        }
        if (moveLeft) {
            moveX += lookZ * MOVE_SPEED;
            moveZ -= lookX * MOVE_SPEED;
        }
        if (moveRight) {
            moveX -= lookZ * MOVE_SPEED;
            moveZ += lookX * MOVE_SPEED;
        }

        // Horizontal movement
        if (world.isPositionValid(x + moveX, y, z)) x += moveX;
        if (world.isPositionValid(x, y, z + moveZ)) z += moveZ;

        // Vertical movement & collision
        if (world.isPositionValid(x, y + velY, z)) {
            y += velY;
            onGround = false;
        } else {
            if (velY < 0) onGround = true;
            velY = 0;
        }

        // Jumping
        if (jump && onGround) {
            velY = JUMP_FORCE;
            onGround = false;
            jump = false;
        }

        updateLookVector();
    }
}