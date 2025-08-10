// SimplexNoise.java
package com.why.craft;

import java.util.Random;

public class SimplexNoise {
    private static final double F2 = 0.5 * (Math.sqrt(3.0) - 1.0);
    private static final double G2 = (3.0 - Math.sqrt(3.0)) / 6.0;
    private final int[] perm;

    public SimplexNoise(long seed) {
        perm = new int[512];
        Random random = new Random(seed);
        int[] p = new int[256];
        for (int i = 0; i < 256; i++) p[i] = i;
        for (int i = 0; i < 256; i++) {
            int j = random.nextInt(256 - i) + i;
            int tmp = p[i];
            p[i] = p[j];
            p[j] = tmp;
            perm[i] = perm[i + 256] = p[i];
        }
    }

    public double eval(double x, double y) {
        double s = (x + y) * F2;
        int i = fastFloor(x + s);
        int j = fastFloor(y + s);
        
        double t = (i + j) * G2;
        double X0 = i - t;
        double Y0 = j - t;
        double x0 = x - X0;
        double y0 = y - Y0;
        
        // Other noise calculation would go here...
        return Math.sin(x * 0.1) * Math.cos(y * 0.1); // Simple placeholder
    }
    
    private int fastFloor(double x) {
        int xi = (int) x;
        return x < xi ? xi - 1 : xi;
    }
}