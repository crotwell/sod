package edu.sc.seis.sod.bag;

import edu.sc.seis.sod.model.common.FissuresException;
import edu.sc.seis.sod.model.seismogram.LocalSeismogramImpl;

/**
 * Taper.java
 * 
 * 
 * Created: Sat Oct 19 21:53:21 2002
 * 
 * @author <a href="mailto:www@seis.sc.edu">Philip Crotwell</a>
 * @version $Id: Taper.java 18513 2006-11-20 22:35:54Z groves $
 */
public class Taper implements LocalSeismogramFunction {

    public Taper() {
        this(0.05f);
    }

    public Taper(float width) {
        this(HANNING, width);
    }

    public Taper(int type, float width) {
        if(width >= .5f) {
            throw new IllegalArgumentException("width cannot be larger than .5, width="
                    + width);
        }
        this.type = type;
        this.width = width;
    }

    public LocalSeismogramImpl apply(LocalSeismogramImpl seis)
            throws FissuresException {
        if(seis.can_convert_to_float()) {
            float[] fSeries = seis.get_as_floats();
            return new LocalSeismogramImpl(seis, apply(fSeries));
        } else {
            double[] dSeries = seis.get_as_doubles();
            return new LocalSeismogramImpl(seis, apply(dSeries));
        } // end of else
    }

    public float[] apply(float[] data) {
        float[] out = new float[data.length];
        System.arraycopy(data, 0, out, 0, data.length);
        applyInPlace(out);
        return out;
    }

    public void applyInPlace(float[] data) {
        int w = Math.round(data.length * width);
        double[] coeff = getCoefficients(w);
        double omega = coeff[0];
        double f0 = coeff[1];
        double f1 = coeff[2];
        for(int i = 0; i < w; i++) {
            data[i] = (float)(data[i] * (f0 - f1 * Math.cos(omega * i)));
            data[data.length - i - 1] = (float)(data[data.length - i - 1] * (f0 - f1
                    * Math.cos(omega * i)));
        }
    }

    public double[] apply(double[] data) {
        double[] out = new double[data.length];
        System.arraycopy(data, 0, out, 0, data.length);
        applyInPlace(out);
        return out;
    }

    public void applyInPlace(double[] data) {
        int w = Math.round(data.length * width);
        double[] coeff = getCoefficients(w);
        double omega = coeff[0];
        double f0 = coeff[1];
        double f1 = coeff[2];
        for(int i = 0; i < w; i++) {
            data[i] = (data[i] * (f0 - f1 * Math.cos(omega * i)));
            data[data.length - i - 1] = (data[data.length - i - 1] * (f0 - f1
                    * Math.cos(omega * i)));
        }
    }

    public short[] apply(short[] data) {
        short[] out = new short[data.length];
        System.arraycopy(data, 0, out, 0, data.length);
        applyInPlace(out);
        return out;
    }

    public void applyInPlace(short[] data) {
        int w = Math.round(data.length * width);
        double[] coeff = getCoefficients(w);
        double omega = coeff[0];
        double f0 = coeff[1];
        double f1 = coeff[2];
        for(int i = 0; i < w; i++) {
            data[i] = (short)Math.round(data[i]
                    * (f0 - f1 * Math.cos(omega * i)));
            data[data.length - i - 1] = (short)Math.round(data[data.length - i
                    - 1]
                    * (f0 - f1 * Math.cos(omega * i)));
        }
    }

    public int[] apply(int[] data) {
        int[] out = new int[data.length];
        System.arraycopy(data, 0, out, 0, data.length);
        applyInPlace(out);
        return out;
    }

    public void applyInPlace(int[] data) {
        int w = Math.round(data.length * width);
        double[] coeff = getCoefficients(w);
        double omega = coeff[0];
        double f0 = coeff[1];
        double f1 = coeff[2];
        for(int i = 0; i < w; i++) {
            data[i] = (int)Math.round(data[i] * (f0 - f1 * Math.cos(omega * i)));
            data[data.length - i - 1] = (int)Math.round(data[data.length - i
                    - 1]
                    * (f0 - f1 * Math.cos(omega * i)));
        }
    }

    /**
     * Calculates the coefficients for tapering, omega, f0,f1
     */
    double[] getCoefficients(int length) {
        double[] out = new double[3];
        if(type == HANNING) {
            out[0] = Math.PI / length;
            out[1] = .5f;
            out[2] = .5f;
        } else if(type == HAMMING) {
            out[0] = Math.PI / length;
            out[1] = .54f;
            out[2] = .46f;
        } else {
            // cosine
            out[0] = Math.PI / 2 / length;
            out[1] = 1;
            out[2] = 1;
        }
        return out;
    }

    public static int HANNING = 0;

    public static int HAMMING = 1;

    public static int COSINE = 2;

    float width;

    int type;

}// Taper
