package edu.sc.seis.sod.bag;

import edu.sc.seis.sod.model.common.FissuresException;
import edu.sc.seis.sod.model.seismogram.LocalSeismogramImpl;

/**
 * RTrend.java
 * 
 * 
 * Created: Sat Nov 16 16:20:23 2002
 * 
 * @author <a href="mailto:crotwell">Philip Crotwell</a>
 * @version
 */
public class RTrend implements LocalSeismogramFunction {

    public RTrend() {}

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
        Statistics stat = new Statistics(data);
        double[] trend = stat.linearLeastSquares();
        for(int i = 0; i < data.length; i++) {
            data[i] -= (float)(trend[0] + i * trend[1]);
        } // end of for (int i=0; i<data.length; i++)
    }

    public double[] apply(double[] data) {
        double[] out = new double[data.length];
        System.arraycopy(data, 0, out, 0, data.length);
        applyInPlace(out);
        return out;
    }

    public void applyInPlace(double[] data) {
        Statistics stat = new Statistics(data);
        double[] trend = stat.linearLeastSquares();
        for(int i = 0; i < data.length; i++) {
            data[i] -= (trend[0] + i * trend[1]);
        } // end of for (int i=0; i<data.length; i++)
    }

    public int[] apply(int[] data) {
        int[] out = new int[data.length];
        System.arraycopy(data, 0, out, 0, data.length);
        applyInPlace(out);
        return out;
    }

    public void applyInPlace(int[] data) {
        Statistics stat = new Statistics(data);
        double[] trend = stat.linearLeastSquares();
        for(int i = 0; i < data.length; i++) {
            data[i] -= (int)Math.round(trend[0] + i * trend[1]);
        } // end of for (int i=0; i<data.length; i++)
    }

    public short[] apply(short[] data) {
        short[] out = new short[data.length];
        System.arraycopy(data, 0, out, 0, data.length);
        applyInPlace(out);
        return out;
    }

    public void applyInPlace(short[] data) {
        Statistics stat = new Statistics(data);
        double[] trend = stat.linearLeastSquares();
        for(int i = 0; i < data.length; i++) {
            data[i] -= (short)Math.round(trend[0] + i * trend[1]);
        } // end of for (int i=0; i<data.length; i++)
    }
}// RTrend
