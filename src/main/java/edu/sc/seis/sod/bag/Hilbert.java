package edu.sc.seis.sod.bag;

import edu.sc.seis.sod.model.common.FissuresException;
import edu.sc.seis.sod.model.seismogram.LocalSeismogramImpl;


/**
 * See http://www.mers.byu.edu/docs/reports/MERS9505.pdf for info on the hilbert transform.
 * 
 * @author crotwell
 * Created on Apr 25, 2005
 */
public class Hilbert implements LocalSeismogramFunction {

    public Hilbert() {
    }

    public LocalSeismogramImpl apply(LocalSeismogramImpl seis) throws FissuresException {
        Cmplx[] c = Cmplx.fft(seis.get_as_floats());
        for(int i = 0; i < c.length/2; i++) {
            double tmp = c[i].i;
            c[i].i = c[i].r;
            c[i].r = -tmp;
        }
        for(int i = c.length/2; i < c.length; i++) {
            double tmp = c[i].i;
            c[i].i = -c[i].r;
            c[i].r = tmp;
        }
        return new LocalSeismogramImpl(seis, Cmplx.fftInverse(c, seis.getNumPoints()));
    }
    
    public Cmplx[] analyticSignal(LocalSeismogramImpl seis) throws FissuresException {
        float[] seisData = seis.get_as_floats();
        LocalSeismogramImpl hilbert = apply(seis);
        float[] hilbertData = hilbert.get_as_floats();
        Cmplx[] out = new Cmplx[seis.getNumPoints()];
        for(int i = 0; i < out.length; i++) {
            out[i] = new Cmplx(seisData[i], hilbertData[i]);
        }
        return out;
    }
    
    public double[] unwrapPhase(Cmplx[] analytic) {
        double[] out = new double[analytic.length];
        int wraps = 0;
        double a = analytic[0].phs();
        out[0] = a;
        double b = analytic[1].phs();
        out[1] = b;
        double c;
        for(int i = 2; i < out.length; i++) {
            // guess no extra wrapping
            c = analytic[i].phs()+wraps*2*Math.PI;
            if (b - c > Math.PI) {
                // unwrap up
                wraps++;
                c = analytic[i].phs()+wraps*2*Math.PI;
            } else if (b- c < -Math.PI) {
                // unwrap down
                wraps--;
                c = analytic[i].phs()+wraps*2*Math.PI;
            }
            out[i] = c;
            a = b;
            b = c;
        }
        return out;
    }

    public double[] phase(Cmplx[] analytic) {
        double[] phase = new double[analytic.length];
        for(int i = 0; i < analytic.length; i++) {
            phase[i] = analytic[i].phs();
        }
        return phase;
    }
    
    public double[] envelope(Cmplx[] analytic) {
        double[] amp = new double[analytic.length];
        for(int i = 0; i < analytic.length; i++) {
            amp[i] = analytic[i].mag();
        }
        return amp;
    }
    
    public double[] instantFreq(Cmplx[] analytic) {
        double[] phase = new double[analytic.length];
        phase = unwrapPhase(analytic);
        double[] freq = new double[phase.length];
        freq[0] = phase[0];
        for(int i = 1; i < freq.length; i++) {
            //freq[i] = (phase[i]-phase[i-1])/radial.getSampling().getPeriod().getValue(UnitImpl.SECOND);
            freq[i] = (phase[i]-phase[i-1]);
        }
        return freq;
    }
    
}
