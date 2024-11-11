package com.peter.ccgraphics;

public class ColorHelper {

    public static int pack(int r, int g, int b) {
        return 0xff000000 | ((r & 0xff) << 16) | ((g & 0xff) << 8) | (b & 0xff);
    }

    public static int pack(int r, int g, int b, int a) {
        return ((a & 0xff) << 24) | ((r & 0xff) << 16) | ((g & 0xff) << 8) | (b & 0xff);
    }

    public static int[] unpackRGBA(int color) {
        int a = color >> 24 & 0xff;
        int r = color >> 16 & 0xff;
        int g = color >> 8 & 0xff;
        int b = color & 0xff;
        return new int[] { r, g, b, a };
    }

    public static int convert(double[] color) {
        int r = (int)(color[1] * 255);
        int g = (int)(color[2] * 255);
        int b = (int)(color[3] * 255);
        return pack(r, g, b);
    }

    public static int convert(byte[] color) {
        int r = color[0];
        int g = color[1];
        int b = color[2];
        return pack(r, g, b);
    }
}
