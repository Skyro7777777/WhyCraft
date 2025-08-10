package com.why.craft;

public class Frustum {
    private float[][] planes = new float[6][4];
    
    public void update(float[] clipMatrix) {
        // Extract frustum planes from clip matrix
        planes[0][0] = clipMatrix[3] - clipMatrix[0];
        planes[0][1] = clipMatrix[7] - clipMatrix[4];
        planes[0][2] = clipMatrix[11] - clipMatrix[8];
        planes[0][3] = clipMatrix[15] - clipMatrix[12];
        
        planes[1][0] = clipMatrix[3] + clipMatrix[0];
        planes[1][1] = clipMatrix[7] + clipMatrix[4];
        planes[1][2] = clipMatrix[11] + clipMatrix[8];
        planes[1][3] = clipMatrix[15] + clipMatrix[12];
        
        planes[2][0] = clipMatrix[3] + clipMatrix[1];
        planes[2][1] = clipMatrix[7] + clipMatrix[5];
        planes[2][2] = clipMatrix[11] + clipMatrix[9];
        planes[2][3] = clipMatrix[15] + clipMatrix[13];
        
        planes[3][0] = clipMatrix[3] - clipMatrix[1];
        planes[3][1] = clipMatrix[7] - clipMatrix[5];
        planes[3][2] = clipMatrix[11] - clipMatrix[9];
        planes[3][3] = clipMatrix[15] - clipMatrix[13];
        
        planes[4][0] = clipMatrix[3] - clipMatrix[2];
        planes[4][1] = clipMatrix[7] - clipMatrix[6];
        planes[4][2] = clipMatrix[11] - clipMatrix[10];
        planes[4][3] = clipMatrix[15] - clipMatrix[14];
        
        planes[5][0] = clipMatrix[3] + clipMatrix[2];
        planes[5][1] = clipMatrix[7] + clipMatrix[6];
        planes[5][2] = clipMatrix[11] + clipMatrix[10];
        planes[5][3] = clipMatrix[15] + clipMatrix[14];
        
        // Normalize planes
        for (int i = 0; i < 6; i++) {
            float length = (float) Math.sqrt(
                planes[i][0] * planes[i][0] + 
                planes[i][1] * planes[i][1] + 
                planes[i][2] * planes[i][2]);
            
            planes[i][0] /= length;
            planes[i][1] /= length;
            planes[i][2] /= length;
            planes[i][3] /= length;
        }
    }
    
    public boolean isBoxVisible(float minX, float minY, float minZ, 
                                float maxX, float maxY, float maxZ) {
        for (int i = 0; i < 6; i++) {
            if (planes[i][0] * minX + planes[i][1] * minY + planes[i][2] * minZ + planes[i][3] <= 0 &&
                planes[i][0] * maxX + planes[i][1] * minY + planes[i][2] * minZ + planes[i][3] <= 0 &&
                planes[i][0] * minX + planes[i][1] * maxY + planes[i][2] * minZ + planes[i][3] <= 0 &&
                planes[i][0] * maxX + planes[i][1] * maxY + planes[i][2] * minZ + planes[i][3] <= 0 &&
                planes[i][0] * minX + planes[i][1] * minY + planes[i][2] * maxZ + planes[i][3] <= 0 &&
                planes[i][0] * maxX + planes[i][1] * minY + planes[i][2] * maxZ + planes[i][3] <= 0 &&
                planes[i][0] * minX + planes[i][1] * maxY + planes[i][2] * maxZ + planes[i][3] <= 0 &&
                planes[i][0] * maxX + planes[i][1] * maxY + planes[i][2] * maxZ + planes[i][3] <= 0) {
                return false;
            }
        }
        return true;
    }
}