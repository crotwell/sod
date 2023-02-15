package edu.sc.seis.sod.bag;

import edu.sc.seis.seisFile.sac.Complex;
import edu.sc.seis.seisFile.sac.SacPoleZero;
import edu.sc.seis.sod.model.common.FissuresException;
import edu.sc.seis.sod.model.common.UnitImpl;
import edu.sc.seis.sod.model.seismogram.LocalSeismogramImpl;

/**
 * <pre>
 * 
 *  
 *   
 *     Ok, I'll try outline as I am stumbling with java....
 *    
 *     We don't need to do the general transfer with all options,
 *     only deconvolve the pole-zero (or resp if you want the decimation
 *     filters, but I don't see that as useful)
 *    
 *     read sac file
 *    
 *     remove mean
 *     taper? optional?
 *    
 *     find next large power of 2 length
 *     2&circ;next2 &gt; length of sac file (so if sac file is 2&circ;n got 2&circ;(n+1)
 *    
 *     zeropad to 2&circ;next2
 *     fft =&gt; Signal(f)
 *    
 *     read pole-zero file (if its a real sac pole zero file,
 *     its already in radians, and its Counts/Meter so its divided to remove)
 *    
 *     evaluate at pole-zero file for s=i*2*pi*f 
 *    
 *     (s-z1)(s-z2)...(s-zn)
 *     _____________________    = PZ(s)
 *     (s-p1)(s-p2)...(s-pm)
 *    
 *     where the f are (0,delf,delf*2,...,Fnyquist) and use the conjugate
 *     symmetry as both data and response are real time functions to get the
 *     &quot;negative frequency&quot; values in the second half of the FFT output, unless
 *     its a real fft and stops at Fnyquist.
 *    
 *     note that PZ(s) is probably == 0+0i for most instruments, so beware
 *     and divide by zero issues... it should not generally be 0+0i elsewhere.
 *    
 *     divide Signal(f) by PS(s)
 *    
 *     apply taper as in sac transfer f1, f2, f3, f4
 *     0 below f1 and above f4
 *     linear from 01 to 1 between f1 and f2 and f3 and f4
 *     unity between f2 and f3
 *     to get rid of noise amplification where PZ(s) is small
 *    
 *     Inverse FFT
 *    
 *     (FFT normalization as needed so its a FFT - IFFT identity)
 *    
 *     I think here we should have a sac file to write out
 *     with new header, max.min,mean updated and the rest is
 *     ok. Don't think sac files routinely track data as counts or
 *     physical units....
 *    
 *     GR
 *    
 *   
 *  
 * </pre>
 * 
 * @author crotwell Created on Jul 22, 2005
 */
public class Transfer {

    public LocalSeismogramImpl apply(LocalSeismogramImpl seis,
                                     SacPoleZero sacPoleZero,
                                     float lowCut,
                                     float lowPass,
                                     float highPass,
                                     float highCut) throws FissuresException {
        PoleZeroTranslator poleZero = new PoleZeroTranslator(sacPoleZero);
        double sampFreq = seis.getSampling()
        .getFrequency()
        .getValue(UnitImpl.HERTZ);
        
        float[] values = seis.get_as_floats();
        /* sac premultiplies the data by the sample period before doing the fft. Later it
         * seems to be cancled out by premultiplying the pole zeros by a similar factor.
         * I don't understand why they do this, but am reporducing it in order to be 
         * compatible.
         */
        for(int i = 0; i < values.length; i++) {
            values[i] /= (float)sampFreq;
        }
        Cmplx[] freqValues = Cmplx.fft(values);
        freqValues = combine(freqValues, sampFreq, poleZero, lowCut, lowPass, highPass, highCut);
        
        values = Cmplx.fftInverse(freqValues, values.length);
        // a extra factor of nfft gets in somehow???
        for(int i = 0; i < values.length; i++) {
            values[i] *= freqValues.length;
        }
        LocalSeismogramImpl out = new LocalSeismogramImpl(seis, values);
        out.y_unit = UnitImpl.METER;
        return out;
    }
    
    
    static Cmplx[] combine(Cmplx[] freqValues, double sampFreq, PoleZeroTranslator poleZero, float lowCut,
                    float lowPass,
                    float highPass,
                    float highCut) {

        double deltaF = sampFreq / freqValues.length;
        double freq;
        // handle zero freq
        freqValues[0] = ZERO;
        // handle nyquist
        freq = sampFreq / 2;
        Cmplx respAtS = evalPoleZeroInverse(poleZero, freq);
        respAtS = Cmplx.mul(respAtS, deltaF*freqTaper(freq,
                                               lowCut,
                                               lowPass,
                                               highPass,
                                               highCut));
        freqValues[freqValues.length / 2] = Cmplx.mul(freqValues[freqValues.length / 2],
                                                      respAtS);
        for(int i = 1; i < freqValues.length / 2; i++) {
            freq = i * deltaF;
            respAtS = evalPoleZeroInverse(poleZero, freq);
            // fft in sac has opposite sign on imag, so take conjugate to make same
            respAtS = Cmplx.mul(respAtS, deltaF*freqTaper(freq,
                                                               lowCut,
                                                               lowPass,
                                                               highPass,
                                                               highCut));
            freqValues[i] = Cmplx.mul(freqValues[i], respAtS);
            freqValues[freqValues.length - i] = freqValues[i].conjg();
        }
        return freqValues;
    }

    /**
     * Evaluates the poles and zeros at the given value. The return value is
     * 1/(pz(s) to avoid divide by zero issues. If there is a divide by zero
     * situation, then the response is set to be 0+0i.
     */
    public static Cmplx evalPoleZeroInverse(PoleZeroTranslator pz, double freq) {
        Cmplx s = new Cmplx(0, 2 * Math.PI * freq);
        Cmplx zeroOut = new Cmplx(1, 0);
        Cmplx poleOut = new Cmplx(1, 0);
        for(int i = 0; i < pz.getPoles().length; i++) {
            poleOut = Cmplx.mul(poleOut, Cmplx.sub(s, pz.getPoles()[i]));
        }
        for(int i = 0; i < pz.getZeros().length; i++) {
            if(s.real() == pz.getZeros()[i].real()
                    && s.imag() == pz.getZeros()[i].imag()) {
                return ZERO;
            }
            zeroOut = Cmplx.mul(zeroOut, Cmplx.sub(s, pz.getZeros()[i]));
        }
        Cmplx out = Cmplx.div(poleOut, zeroOut);
        // sac uses opposite sign in imag, so take conjugate
        return Cmplx.div(out, pz.getConstant()).conjg();
    }

    public static double freqTaper(double freq,
                                   float lowCut,
                                   float lowPass,
                                   float highPass,
                                   float highCut) {
        if (lowCut > lowPass || lowPass > highPass || highPass > highCut) {
            throw new IllegalArgumentException("must be lowCut > lowPass > highPass > highCut: "+lowCut +" "+ lowPass +" "+ highPass +" "+ highCut);
        }
        if(freq <= lowCut || freq >= highCut) {
            return 0;
        }
        if(freq >= lowPass && freq <= highPass) {
            return 1;
        }
        if(freq > lowCut && freq < lowPass) {
            return 0.5e0 * (1.0e0 + Math.cos(Math.PI * (freq - lowPass)
                    / (lowCut - lowPass)));
        }
        // freq > highPass && freq < highCut
        return 0.5e0 * (1.0e0 - Math.cos(Math.PI * (freq - highCut)
                / (highPass - highCut)));
    }

    static final Cmplx ZERO = new Cmplx(0, 0);
}

/** 
 * This is to translate from a SacPoleZero file, which uses the seisFile.sac.Complex class into
 * something that uses the Cmplx class. Dumb, dumb, dumb. Why didn't java come with a stupid
 * complex number class!!!
 * 
 * @author crotwell
 * 
 * Created on Oct 20, 2010
 */
class PoleZeroTranslator {
    PoleZeroTranslator(SacPoleZero spz) {
        this.constant = spz.getConstant();
        this.poles = transArray(spz.getPoles());
        this.zeros = transArray(spz.getZeros());
    }
    
    static Cmplx[] transArray(Complex[] c) {
        Cmplx[] out = new Cmplx[c.length];
        for (int i = 0; i < c.length; i++) {
            out[i] = new Cmplx(c[i].getReal(), c[i].getImaginary());
        }
        return out;
    }
 
    Cmplx[] getPoles() {
        return poles;
    }
    
    Cmplx[] getZeros() {
        return zeros;
    }
    
    float getConstant() {
        return constant;
    }
    
    Cmplx[] poles;
    Cmplx[] zeros;
    float constant;
}