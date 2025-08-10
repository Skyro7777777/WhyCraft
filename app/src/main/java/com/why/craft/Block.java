package com.why.craft;

public class Block {
    public static final int AIR = 0;
    public static final int GRASS = 1;
    public static final int DIRT = 2;
    public static final int STONE = 3;
    
    public static float[] getColor(int type) {
        switch (type) {
            case GRASS: return new float[]{0.1f, 0.7f, 0.1f}; // Darker green
            case DIRT: return new float[]{0.4f, 0.25f, 0.05f}; // Darker brown
            case STONE: return new float[]{0.4f, 0.4f, 0.4f}; // Darker gray
            default: return new float[]{1, 1, 1};
        }
    }
}