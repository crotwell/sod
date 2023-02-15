package edu.sc.seis.sod.bag;

import com.oregondsp.signalProcessing.fft.RDFT;

public class OregonDspFFT {

    public static float[] forward(float[] realData) {
        int N = 32;
        int log2N = 5;
        while(N < realData.length) { log2N +=1; N *= 2;}
        float[] in = new float[N];
        float[] out = new float[N];
        System.arraycopy(realData, 0, in, 0, realData.length);
        RDFT rdft = new RDFT(log2N);
        rdft.evaluate(in, out);
        return out;
    }

    /**
     * Performs the inverse fft operation of the realFFT call.
     */
    public static float[] inverse(float[] realData) {
        int N = 32;
        int log2N = 5;
        while(N < realData.length) { log2N +=1; N *= 2;}
        float[] in = new float[N];
        float[] out = new float[N];
        System.arraycopy(realData, 0, in, 0, realData.length);
        RDFT rdft = new RDFT(log2N);
        rdft.evaluateInverse(in, out);
        return out;
    }

    public static float[] correlate(float[] x, float[] y) {
        int N = 32;
        int log2N = 5;
        while(N < x.length) { log2N +=1; N *= 2;}
        while(N < y.length) { log2N +=1; N *= 2;}
        float[] xIn = new float[N];
        float[] yIn = new float[N];
        System.arraycopy(x, 0, xIn, 0, x.length);
        System.arraycopy(y, 0, yIn, 0, y.length);
        RDFT rdft = new RDFT(log2N);
        float[] xforward = new float[N];
        rdft.evaluate(xIn, xforward);
        xIn = null; // memory
        float[] yforward = new float[N];
        rdft.evaluate(yIn, yforward);
        yIn = null; // memory
        float[] ans = new float[N];
        // handle 0 and nyquist
        ans[0] = xforward[0] * yforward[0];
        ans[N / 2] = xforward[N / 2] * yforward[N / 2];
        float a, b, c, d;
        for (int j = 1; j < N / 2; j++) {
            a = xforward[j];
            b = xforward[N - j];
            c = yforward[j];
            d = yforward[N - j];
            ans[j] = a * c + b * d;
            ans[N - j] = -a * d + b * c;
        }
        xforward = null;
        yforward = null;
        float[] out = new float[N];
        rdft.evaluateInverse(ans, out);
        return out;
    }

    public static float[] convolve(float[] x, float[] y, float delta) {
        int N = 32;
        int log2N = 5;
        while(N < x.length) { log2N +=1; N *= 2;}
        while(N < y.length) { log2N +=1; N *= 2;}
        float[] xIn = new float[N];
        float[] yIn = new float[N];
        System.arraycopy(x, 0, xIn, 0, x.length);
        System.arraycopy(y, 0, yIn, 0, y.length);
        RDFT rdft = new RDFT(log2N);
        float[] xforward = new float[N];
        rdft.evaluate(xIn, xforward);
        xIn = null; // memory
        float[] yforward = new float[N];
        rdft.evaluate(yIn, yforward);
        yIn = null; // memory
        
        
        float[] ans = new float[N];
        // handle 0 and nyquist
        ans[0] = xforward[0] * yforward[0];
        ans[N / 2] = xforward[N / 2] * yforward[N / 2];
        float a, b, c, d;
        for (int j = 1; j < N / 2; j++) {
            a = xforward[j];
            b = xforward[N - j];
            c = yforward[j];
            d = yforward[N - j];
            ans[j] = a * c - b * d;
            ans[N - j] = a * d + b * c;
        }
        float[] out = new float[N];
        rdft.evaluateInverse(ans, out);
        for (int i = 0; i < ans.length; i++) {
            out[i] *= delta;
        }
        return out;
    }
}
