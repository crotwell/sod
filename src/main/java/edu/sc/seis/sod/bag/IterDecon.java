package edu.sc.seis.sod.bag;



/**
 * IterDecon.java
 * 
 * Java version of fortran iterdecon by Chuck Ammon
 * http://eqseis.geosc.psu.edu/~cammon/HTML/RftnDocs/rftn01.html
 * 
 * Also see 
 * Ligorria, J., and C. Ammon, Iterative deconvolution and receiver-function estimation, 
 * Bull., Seis. Soc. Am., 89 (5), 1395-1400, 1999. 
 *
 *
 * Created: Sat Mar 23 18:24:29 2002
 *
 * @author Philip Crotwell * @version $Id: IterDecon.java 21100 2010-02-23 19:25:21Z crotwell $
 */

public class IterDecon {
    public IterDecon (int maxBumps,
                      boolean useAbsVal,
                      float tol,
                      float gwidthFactor) {
        this.maxBumps = maxBumps;
        this.useAbsVal = useAbsVal;
        this.tol = tol;
        this.gwidthFactor = gwidthFactor;
    }

    public IterDeconResult process(float[] numerator,
                                   float[] denominator,
                                   float dt) throws ZeroPowerException {
        float[] amps = new float[maxBumps];
        int[] shifts = new int[maxBumps];

        numerator = makePowerTwo(numerator);
        denominator = makePowerTwo(denominator);

        /* Now begin the cross-correlation procedure
         Put the filter in the signals
         */
        float[] f  = gaussianFilter(numerator, gwidthFactor, dt);
        float[] g  = gaussianFilter(denominator, gwidthFactor, dt);

        // compute the power in the "numerator" for error scaling
        float fPower = power(f);
        float prevPower = fPower;
        float gPower = power(g);
        if (fPower == 0 || gPower == 0) {
            throw new ZeroPowerException("Power of numerator and denominator must be non-zero: num="+fPower+" denom="+gPower);
        }
        float[] residual = f;
        float[] predicted = new float[0];

        float[][] corrSave = new float[maxBumps][];
        float improvement = 100;
        int bump;
        for ( bump=0; bump < maxBumps && improvement > tol ; bump++) {

            // correlate the signals
            float[] corr = correlateNorm(residual, g);
            corrSave[bump] = corr;

            //  find the peak in the correlation
            if (useAbsVal) {
                shifts[bump] = getAbsMaxIndex(corr);
            } else {
                shifts[bump] = getMaxIndex(corr);
            } // end of else
            amps[bump] = corr[shifts[bump]]/dt; // why normalize by dt here?

            predicted = buildDecon(amps, shifts, g.length, gwidthFactor, dt);
            float[] predConvolve;
            if (useNativeFFT) {
                throw new RuntimeException("NativeFFT not implemented");
                //predConvolve = NativeFFT.convolve(predicted, denominator, dt);
            } else {
                predConvolve = Cmplx.convolve(predicted, denominator, dt);
            }

            residual = getResidual(f, predConvolve);
            float residualPower = power(residual);
            improvement = 100*(prevPower-residualPower)/fPower;
            prevPower = residualPower;
        } // end of for (int bump=0; bump < maxBumps; bump++)

        IterDeconResult result = new IterDeconResult(maxBumps,
                                                     useAbsVal,
                                                     tol,
                                                     gwidthFactor,
                                                     numerator,
                                                     denominator,
                                                     dt,
                                                     amps,
                                                     shifts,
                                                     residual,
                                                     predicted,
                                                     corrSave,
                                                     buildSpikes(amps, shifts, g.length),
                                                     prevPower,
                                                     fPower,
                                                     bump);
        return result;
    }

    /** computes the correlation of f and g normalized by the zero-lag
     *  autocorrelation of g. */
    public static float[] correlateNorm(float[] fdata, float[] gdata) {
        float zeroLag = power(gdata);

        float[] corr;
        if (useNativeFFT) {
            throw new RuntimeException("NativeFFT not implemented");
            //corr = NativeFFT.correlate(fdata, gdata);
        } else {
            corr = Cmplx.correlate(fdata, gdata);
        }

        float temp =1 / zeroLag;
        for (int i=0; i<corr.length; i++) {
            corr[i] *= temp;
        }
        return corr;
    }

    public static float[] buildSpikes(float[] amps, int[] shifts, int n) {
        float[] p = new float[n];
        for (int i=0; i<amps.length; i++) {
            p[shifts[i]] += amps[i];
        } // end of for (int i=0; i<amps.length; i++)
        return p;
    }

    public static float[] buildDecon(float[] amps, int[] shifts, int n, float gwidthFactor, float dt) {
        return gaussianFilter(buildSpikes(amps, shifts, n), gwidthFactor, dt);
    }

    /** returns the residual, ie x-y */
    public static float[] getResidual(float[] x, float[] y) {
        float[] r = new float[x.length];
        for (int i=0; i<x.length; i++) {
            r[i] = x[i]-y[i];
        } // end of for (int i=0; i<x.length; i++)
        return r;
    }

    public static int getAbsMaxIndex(float[] data) {
        int minIndex = getMinIndex(data);
        int maxIndex = getMaxIndex(data);
        if (Math.abs(data[minIndex]) > Math.abs(data[maxIndex])) {
            return minIndex;
        } // end of if (Math.abs(data[minIndex]) > Math.abs(data[maxIndex]))
        return maxIndex;
    }

    public static final int getMinIndex(float[] data) {
        int index = 0;
        // iterate only to length/2 to avoid negative lag correlations
        for (int i=1; i<data.length/2; i++) {
            if (data[i] < data[index]) {
                index = i;
            }
        }
        return index;
    }

    public static final int getMaxIndex(float[] data) {
        int index = 0;
        // iterate only to length/2 to avoid negative lag correlations
        for (int i=1; i<data.length/2; i++) {
            if (data[i] > data[index]) {
                index = i;
            }
        }
        return index;
    }


    public static final float power(float[] data) {
        float power=0;
            for (int i=0; i<data.length; i++) {
                power += data[i]*data[i];
            } // end of for (int i=0; i<data.length; i++)
        
        return power;
    }

    /** convolve a function with a unit-area Gaussian filter.
     *   G(w) = exp(-w^2 / (4 a^2))
     *  The 1D gaussian is: f(x) = 1/(2*PI*sigma) e^(-x^2/(q * sigma^2))
     *  and the impluse response is: g(x) = 1/(2*PI)e^(-sigma^2 * u^2 / 2)
     *
     * If gwidthFactor is zero, does not filter.
     */
    public static float[] gaussianFilter(float[] x,
                                         float gwidthFactor,
                                         float dt) {
        return gaussianFilter(x, gwidthFactor, dt, new float[x.length]);
    }

    public static float[] gaussianFilter(float[] x,
                                         float gwidthFactor,
                                         float dt,
                                         float[] gaussVals) {
        // gwidthFactor of zero means no filter
        if (gwidthFactor == 0) {
            return x;
        }
        float[] forward = forwardFFT(x);
        double df = 1/(x.length * dt);
        double d_omega = 2*Math.PI*df;
        double gwidth = 4*gwidthFactor*gwidthFactor;
        double gauss;
        double omega;

        // Handle the nyquist frequency
        omega = Math.PI/dt; // eliminate 2 / 2
        gauss = Math.exp(-omega*omega / gwidth);
        gaussVals[0] = 1;
        if (useOregonDSPFFT) {
            forward[forward.length/2] *= gauss;
        } else {
            forward[1] *= gauss;
        }
        int j;
        for (int i=1; i<forward.length/2; i++) {
            j  = i*2;
            omega = i*d_omega;
            gauss = Math.exp(-omega*omega / gwidth);
            gaussVals[i] = (float)gauss;
            
            if (useOregonDSPFFT) {
                forward[i] *= gauss;
                forward[forward.length-i] *= gauss;
            } else {
                forward[j] *= gauss;
                forward[j+1] *= gauss;
            }
        }
        forward = inverseFFT(forward);
        return forward;
    }
    

    public static float[] gaussianFilterNoFFT(float[] forward,
                                         float gwidthFactor,
                                         float dt,
                                         float[] gaussVals) {
        double df = 1/(forward.length * dt);
        double d_omega = 2*Math.PI*df;
        double gwidth = 4*gwidthFactor*gwidthFactor;
        double gauss;
        double omega;

        // Handle the nyquist frequency
        omega = Math.PI/dt; // eliminate 2 / 2
        gauss = Math.exp(-omega*omega / gwidth);
        if (useOregonDSPFFT) {
            forward[forward.length/2] *= gauss;
        } else {
            forward[1] *= gauss;
        }
        gaussVals[0] = 1;

        int j;
        for (int i=1; i<forward.length/2; i++) {
            omega = i*d_omega;
            gauss = Math.exp(-omega*omega / gwidth);
            gaussVals[i] = (float)gauss;
            if (useOregonDSPFFT) {
                forward[i] *= gauss;
                forward[forward.length-i] *= gauss;
            } else {
                j  = i*2;
                forward[j] *= gauss;
                forward[j+1] *= gauss;
                if (i < 32) {
                System.out.println("IterDecon "+i+" "+j+" "+gauss+" "+forward[j]+" "+forward[j+1]);
                }
            }
        }
        return forward;
    }
    
    public static float[] shortenFFT(float[] tmp) {
        float[] forward = new float[tmp.length/2];
        System.arraycopy(tmp, 0, forward, 0, forward.length);
        forward[0] = tmp[0];
        forward[1] = tmp[tmp.length/2];
        return forward;
    }

    public static float[] lengthenFFT(float[] tmp) {
        float[] out = new float[tmp.length*2];
        for(int i = 1; i < tmp.length/2; i++) {
            out[2*i] = tmp[2*i];
            out[2*i+1] = tmp[2*i+1];
            out[out.length-2*i] = tmp[2*i];
            out[out.length-2*i+1] = -1*tmp[2*i+1];
        }
        out[0] = tmp[0];
        out[1] = 0;
        out[tmp.length] = tmp[1];
        out[tmp.length+1]=0;
        return out;
    }
    
    public static float[] phaseShift(float[] x, float shift, float dt) {

        float[] forward;
        forward = forwardFFT(x);

        double df = 1/(forward.length * dt);
        double d_omega = 2*Math.PI*df;

        double omega;
        //Handle the nyquist frequency
        omega = Math.PI/dt;
        if (useOregonDSPFFT) {
            forward[forward.length/2] *= (float)Math.cos(omega*shift);
        } else {
            forward[1] *= (float)Math.cos(omega*shift);
        }

        double a,b,c,d;
        for (int j=1; j<forward.length/2; j++) {
            omega = (j)*d_omega;
            c = Math.cos(omega*shift);
            d = Math.sin(omega*shift);
            if (useOregonDSPFFT) {
                a = forward[j];
                b = forward[forward.length-j];
                forward[j] = (float)(a*c-b*d);
                forward[forward.length-j] = (float)(a*d+b*c);
            } else {
                a = forward[2*j];
                b = forward[2*j+1];
                forward[2*j] = (float)(a*c-b*d);
                forward[2*j+1] = (float)(a*d+b*c);
            }
        }

        forward = inverseFFT(forward);
        return forward;
    }
    
    public static float[] forwardFFT(float[] x) {
        float[] forward = new float[x.length];
        System.arraycopy(x, 0, forward, 0, x.length);
        if(useNativeFFT) {
           // NativeFFT.forward(forward);
            throw new RuntimeException("NativeFFT not implemented");
            
        } else if (useOregonDSPFFT) {
            forward = OregonDspFFT.forward(x);
            // OregonDSP uses opposite sign convention as Num. Rec. FFT, so
            // values are complex congugate of what we want
            for (int i = 1; i < forward.length/2; i++) {
                forward[forward.length-i] *= -1;
            }
        } else {
            // not on mac, so no altavec fft
            float[] javaFFT = new float[forward.length*2];
            for(int i = 0; i < forward.length; i++) {
                javaFFT[2*i] = forward[i];
            }
            forward = shortenFFT(Cmplx.four1Forward(javaFFT));
            // Cmplx fft does 2n values, but native does n using symmetry
            
        }
        return forward;
    }
    
    public static float[] inverseFFT(float[] x) {
        float[] inverse = new float[x.length];
        System.arraycopy(x, 0, inverse, 0, x.length);
        if(useNativeFFT) {
            throw new RuntimeException("NativeFFT not implemented");
            //NativeFFT.inverse(inverse);
        } else if (useOregonDSPFFT) {
            // OregonDSP uses opposite sign convention as Num. Rec. FFT, so
            // values are complex congugate of what we want
            for (int i = 1; i < x.length/2; i++) {
                x[x.length-i] *= -1;
            }
            inverse = OregonDspFFT.inverse(x);;
        } else {
            // not on mac, so no altavec fft
            float[] tmp = Cmplx.four1Inverse(lengthenFFT(inverse));
            inverse = new float[tmp.length/2];
            for(int i = 0; i < tmp.length/2; i++) {
                inverse[i] = tmp[2*i];
            }
        }
        return inverse;
    }

    public static float[] makePowerTwo(float[] data) {
        float[] out = new float[nextPowerTwo(data.length)];
        System.arraycopy(data, 0, out, 0, data.length);
        return out;
    }

    public static int nextPowerTwo(int n) {
        int i=1;
        while (i < n) {
            i*=2;
        }
        return i;
    }

    protected int maxBumps;
    protected boolean useAbsVal;
    protected float tol;
    protected float gwidthFactor;
    public static boolean useNativeFFT = false;
    public static boolean useOregonDSPFFT = true;
//    
//    static {
//        try {
//        NativeFFT nativeFFT = new NativeFFT();
//        } catch (UnsatisfiedLinkError e) {
//            // not on mac, so no altavec fft
//            useNativeFFT = false;
//        }
//    }

}// IterDecon

