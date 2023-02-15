// added by HPC to put in a package
// package net.alomax.freq;
// change package
package edu.sc.seis.sod.bag;

/*
 * This file is part of the Anthony Lomax Java Library. Copyright (C) 1999
 * Anthony Lomax <lomax@faille.unice.fr> This program is free software; you can
 * redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version. This program is distributed
 * in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details. You should have received
 * a copy of the GNU General Public License along with this program; if not,
 * write to the Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 * Boston, MA 02111-1307, USA.
 */
@Deprecated
public class Cmplx {

    public double r; // real part

    public double i; // imaginary part

    private static final double PI = Math.PI;

    private static final double TWOPI = 2.0 * Math.PI;

    Cmplx() {}

    public Cmplx(double re, double im) {
        this.r = re;
        this.i = im;
    }

    /* class methods */
    public static final Cmplx add(double a, Cmplx b) {
        return add(new Cmplx(a, 0), b);
    }

    public static final Cmplx add(Cmplx a, double b) {
        return add(a, new Cmplx(b, 0));
    }

    public static final Cmplx add(Cmplx a, Cmplx b) {
        Cmplx c = new Cmplx();
        c.r = a.r + b.r;
        c.i = a.i + b.i;
        return c;
    }

    public static final Cmplx sub(double a, Cmplx b) {
        return Cmplx.sub(new Cmplx(a, 0), b);
    }

    public static final Cmplx sub(Cmplx a, double b) {
        return Cmplx.sub(a, new Cmplx(b, 0));
    }

    public static final Cmplx sub(Cmplx a, Cmplx b) {
        Cmplx c = new Cmplx();
        c.r = a.r - b.r;
        c.i = a.i - b.i;
        return c;
    }

    public static final Cmplx mul(double a, Cmplx b) {
        return Cmplx.mul(new Cmplx(a, 0), b);
    }

    public static final Cmplx mul(Cmplx a, double b) {
        return Cmplx.mul(a, new Cmplx(b, 0));
    }

    public static final Cmplx mul(Cmplx a, Cmplx b) {
        Cmplx c = new Cmplx();
        c.r = a.r * b.r - a.i * b.i;
        c.i = a.i * b.r + a.r * b.i;
        return c;
    }

    public static final Cmplx div(double a, Cmplx b) {
        return div(new Cmplx(a, 0), b);
    }

    public static final Cmplx div(Cmplx a, double b) {
        return div(a, new Cmplx(b, 0));
    }

    public static final Cmplx div(Cmplx a, Cmplx b) {
        Cmplx c = new Cmplx();
        double r, den;
        if(Math.abs(b.r) >= Math.abs(b.i)) {
            r = b.i / b.r;
            den = b.r + r * b.i;
            c.r = (a.r + r * a.i) / den;
            c.i = (a.i - r * a.r) / den;
        } else {
            r = b.r / b.i;
            den = b.i + r * b.r;
            c.r = (a.r * r + a.i) / den;
            c.i = (a.i * r - a.r) / den;
        }
        return c;
    }

    /* instance methods */
    public final double real() {
        double r;
        return (r = this.r);
    }

    public final double imag() {
        double r;
        return (r = this.i);
    }

    public final double mag() {
        return (Math.sqrt(this.r * this.r + this.i * this.i));
    }

    /**
     * creates a unit mag complex number. zero is assigned (1,0)
     */
    public final Cmplx unitVector() {
        double mag = mag();
        if(mag != 0) {
            return Cmplx.div(this, mag);
        } else {
            return new Cmplx(1, 0);
        }
    }

    public final Cmplx zeroOrUnitVector() {
        if(r == 0 && i == 0) {
            return new Cmplx(0, 0);
        }
        return unitVector();
    }

    public final double phs() {
        if(this.r == 0.0) {
            if(this.i == 0.0)
                return (0.0);
            else
                return ((this.i / Math.abs(this.i)) * 2.0 * Math.atan(1.0));
        } else {
            return (Math.atan2(this.i, this.r));
        }
    }

    public final Cmplx conjg() {
        Cmplx c = new Cmplx();
        c.r = this.r;
        c.i = -this.i;
        return c;
    }

    public static final Cmplx exp(Cmplx arg) {
        Cmplx c = new Cmplx();
        c.r = Math.exp(arg.r) * Math.cos(arg.i);
        c.i = Math.exp(arg.r) * Math.sin(arg.i);
        return c;
    }

    public final Cmplx sqrt() {
        Cmplx c = new Cmplx();
        double x, y, w, r;
        if((this.r == 0.0) && (this.i == 0.0)) {
            c.r = (c.i = 0.0);
            return (c);
        } else {
            x = Math.abs(this.r);
            y = Math.abs(this.i);
            if(x >= y) {
                r = y / x;
                w = Math.sqrt(x)
                        * Math.sqrt(0.5 * (1.0 + Math.sqrt(1.0 + r * r)));
            } else {
                r = x / y;
                w = Math.sqrt(y)
                        * Math.sqrt(0.5 * (r + Math.sqrt(1.0 + r * r)));
            }
            if(this.r >= 0.0) {
                c.r = w;
                c.i = this.i / (2.0 * w);
            } else {
                c.i = (this.i >= 0.0) ? w : -w;
                c.r = this.i / (2.0 * c.i);
            }
            return (c);
        }
    }

    public boolean isNaN() {
        return Double.isNaN(r) || Double.isNaN(i);
    }

    /** Static processing methods */
    /** Forward Fast Fourier Transform */
    public static final Cmplx[] fft(float[] fdata) {
        // find power of two greater than num of points in fdata
        int nPointsPow2 = 1;
        while(nPointsPow2 < fdata.length)
            nPointsPow2 *= 2;
        // create double array
        double[] data = new double[2 * nPointsPow2];
        // load float data to double array
        int i, j;
        for(i = 0, j = 0; i < fdata.length; i++) {
            data[j++] = (double)fdata[i];
            data[j++] = 0.0;
        }
        for(; j < 2 * nPointsPow2; j++)
            data[j] = 0.0;
        // apply forward FFT
        data = four1(data, 1);
        // create complex array
        Cmplx[] cdata = new Cmplx[nPointsPow2];
        // load double data to complex array
        for(i = 0, j = 0; i < nPointsPow2; i++, j += 2)
            cdata[i] = new Cmplx(data[j], data[j + 1]);
        return (cdata);
    }

    /** Inverse Forward Fast Fourier Transform */
    public static final float[] fftInverse(Cmplx[] cdata, int nPoints) {
        // create double array
        double[] data = new double[2 * cdata.length];
        // load complex data to double array
        for(int i = 0, j = 0; i < cdata.length; i++) {
            data[j++] = cdata[i].real();
            data[j++] = cdata[i].imag();
        }
        // apply inverse FFT
        data = four1(data, -1);
        // create float array
        float[] fdata = new float[nPoints];
        // load double data to float array
        for(int i = 0, j = 0; i < nPoints; i++, j += 2)
            fdata[i] = (float)data[j];
        return (fdata);
    }

    public static final float[] convolve(float[] fdata,
                                         float[] gdata,
                                         float delta) {
        if(fdata.length != gdata.length) {
            throw new IllegalArgumentException("fdata and gdata must have same length. "
                    + fdata.length + " " + gdata.length);
        }
        Cmplx[] fTrans = fft(fdata);
        Cmplx[] gTrans = fft(gdata);
        for(int i = 0; i < fTrans.length; i++) {
            fTrans[i] = Cmplx.mul(fTrans[i], gTrans[i]);
        } // end of for (int i=0; i<gdata.length; i++)
        float[] ans = fftInverse(fTrans, fdata.length);
        for(int i = 0; i < ans.length; i++) {
            ans[i] *= delta;
        }
        return ans;
    }

    /**
     * Computes the correlation of fdata with gdata. The value of the output at
     * index i is the sum over j of fdata[i+j]*gdata[j], although using the FFT
     * is much faster than direct sum.
     * 
     *  http://www.ulib.org/webRoot/Books/Numerical_Recipes/bookcpdf.html
     *      section 13-2
     */
    public static final float[] correlate(float[] fdata, float[] gdata) {
        if(fdata.length != gdata.length) {
            throw new IllegalArgumentException("fdata and gdata must have same length. "
                    + fdata.length + " " + gdata.length);
        }
        Cmplx[] fTrans = fft(fdata);
        Cmplx[] gTrans = fft(gdata);
        for(int i = 0; i < fTrans.length; i++) {
            fTrans[i] = Cmplx.mul(fTrans[i], gTrans[i].conjg());
        } // end of for (int i=0; i<gdata.length; i++)
        return fftInverse(fTrans, fdata.length);
    }
    
    public static float[] four1Forward(float[] data) {
        return four1(data, 1);
    }
    
    public static float[] four1Inverse(float[] data) {
        return four1(data, -1);
    }
    
    public static float[] four1(float[] data, int isign) {
        double[] d = new double[data.length];
        for(int i = 0; i < d.length; i++) {
            d[i] = data[i];
        }
        d = four1(d, isign);
        float[] out = new float[d.length];
        for(int i = 0; i < out.length; i++) {
            out[i] = (float)d[i];
        }
        return out;
    }

    /**
     * Fast Fourier Transform (adapted from Numerical Recipies in C) isign = 1
     * replace data by FFT = -1 replace data by inverse FFT data pseudo-complex
     * array of length nn input as real pairs nn integer power of 2
     */
    public static final double[] four1(double[] data, int isign) {
        int nn = data.length / 2;
        int n, m, j;
        double temp;
        n = nn << 1;
        j = 0;
        for(int i = 0; i < n; i += 2) {
            if(j > i) {
                // swap(data[j], data[i]);
                temp = data[j];
                data[j] = data[i];
                data[i] = temp;
                // swap(data[j + 1], data[i + 1]);
                temp = data[j + 1];
                data[j + 1] = data[i + 1];
                data[i + 1] = temp;
            }
            m = n >> 1;
            while(m >= 2 && j > m - 1) {
                j -= m;
                m >>= 1;
            }
            j += m;
        }
        int i, istep;
        double wtemp, wr, wpr, wpi, wi, theta;
        double tempr, tempi;
        int mmax = 2;
        while(n > mmax) {
            istep = 2 * mmax;
            theta = TWOPI / (isign * mmax);
            wtemp = Math.sin(0.5 * theta);
            wpr = -2.0 * wtemp * wtemp;
            wpi = Math.sin(theta);
            wr = 1.0;
            wi = 0.0;
            for(m = 0; m < mmax; m += 2) {
                for(i = m; i < n; i += istep) {
                    j = i + mmax;
                    tempr = wr * data[j] - wi * data[j + 1];
                    tempi = wr * data[j + 1] + wi * data[j];
                    data[j] = data[i] - tempr;
                    data[j + 1] = data[i + 1] - tempi;
                    data[i] += tempr;
                    data[i + 1] += tempi;
                }
                wr = (wtemp = wr) * wpr - wi * wpi + wr;
                wi = wi * wpr + wtemp * wpi + wi;
            }
            mmax = istep;
        }
        if(isign == -1)
            for(m = 0; m < 2 * nn; m += 2)
                data[m] /= nn;
        return (data);
    }

    public String toString() {
        if(i >= 0) {
            return r + " + i " + i;
        } else {
            return r + " - i " + (-1 * i);
        }
    }
} // End class Cmplx
