package com.why.craft;

import java.util.Random;

public class Chunk {
    public final int[][][] blocks;
    public final int width, height, depth;
    public final int offsetX, offsetZ;
    private boolean dirty = true;
    private boolean ready = false;
    private boolean isEmpty = true;
    
    public Chunk(int width, int height, int depth, int offsetX, int offsetZ) {
        this.width = width;
        this.height = height;
        this.depth = depth;
        this.offsetX = offsetX;
        this.offsetZ = offsetZ;
        this.blocks = new int[width][height][depth];
    }
    
    public boolean contains(int x, int y, int z) {
        return x >= offsetX && x < offsetX + width &&
               z >= offsetZ && z < offsetZ + depth &&
               y >= 0 && y < height;
    }
    
    public void generateTerrain() {
        boolean foundNonAir = false;
        for (int x = 0; x < width; x++) {
            for (int z = 0; z < depth; z++) {
                int worldX = x + offsetX;
                int worldZ = z + offsetZ;
                int groundHeight = (int) (10 + Math.sin(worldX * 0.1) * 2 + Math.cos(worldZ * 0.1) * 2);
                
                for (int y = 0; y < height; y++) {
                    if (y > groundHeight) {
                        blocks[x][y][z] = Block.AIR;
                    } else if (y == groundHeight) {
                        blocks[x][y][z] = Block.GRASS;
                        foundNonAir = true;
                    } else if (y > groundHeight - 3) {
                        blocks[x][y][z] = Block.DIRT;
                        foundNonAir = true;
                    } else {
                        blocks[x][y][z] = Block.STONE;
                        foundNonAir = true;
                    }
                }
            }
        }
        isEmpty = !foundNonAir;
        ready = true;
    }
    
    public int getBlock(int x, int y, int z) {
        if (x < 0 || y < 0 || z < 0 || x >= width || y >= height || z >= depth) 
            return Block.AIR;
        return blocks[x][y][z];
    }
    
    public void setBlock(int x, int y, int z, int type) {
        if (x >= 0 && y >= 0 && z >= 0 && x < width && y < height && z < depth) {
            blocks[x][y][z] = type;
            dirty = true;
            
            // Update empty status
            if (type != Block.AIR) isEmpty = false;
        }
    }
    
    public boolean isReady() {
        return ready;
    }
    
    public boolean isEmpty() {
        return isEmpty;
    }
    
    public boolean isDirty() {
        return dirty;
    }
    
    public void markClean() {
        dirty = false;
    }
    
    public void markDirty() {
        dirty = true;
    }
}