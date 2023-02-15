package edu.sc.seis.sod.bag;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.sc.seis.sod.model.common.FissuresException;
import edu.sc.seis.sod.model.seismogram.LocalSeismogramImpl;

/**
 * RMean.java
 *
 *
 * Created: Sat Oct 19 21:54:26 2002
 *
 * @author <a href="mailto:www@seis.sc.edu">Philip Crotwell</a>
 * @version $Id: RMean.java 22072 2011-02-18 15:43:18Z crotwell $
 */

public class RMean implements LocalSeismogramFunction {

    public LocalSeismogramImpl apply(LocalSeismogramImpl seis) throws FissuresException {
        if (seis.can_convert_to_float()) {
            float[] fSeries = seis.get_as_floats();
            return new LocalSeismogramImpl(seis, removeMean(fSeries));
        } else {
            double[] dSeries = seis.get_as_doubles();
            return new LocalSeismogramImpl(seis, removeMean(dSeries));
        } // end of else
    }

    public static float[] removeMean(float[] data) {
        float[] out = new float[data.length];
        System.arraycopy(data, 0, out, 0, data.length);
        removeMeanInPlace(out);
        return out;
    }

    public static void removeMeanInPlace(float[] data) {
        Statistics stat = new Statistics(data);
        float mean = (float)stat.mean();
        for (int i=0; i<data.length; i++) {
            data[i] -= mean;
        } // end of for (int i=0; i<data.length; i++)

    }

    public static double[] removeMean(double[] data) {
        double[] out = new double[data.length];
        System.arraycopy(data, 0, out, 0, data.length);
        removeMeanInPlace(out);
        return out;
    }

    public static void removeMeanInPlace(double[] data) {
        Statistics stat = new Statistics(data);
        double mean = stat.mean();
        for (int i=0; i<data.length; i++) {
            data[i] -= mean;
        } // end of for (int i=0; i<data.length; i++)
    }

    public static int[] removeMean(int[] data) {
        int[] out = new int[data.length];
        System.arraycopy(data, 0, out, 0, data.length);
        removeMeanInPlace(out);
        return out;
    }

    public static void removeMeanInPlace(int[] data) {
        Statistics stat = new Statistics(data);
        int mean = Math.round((float)stat.mean());
        for (int i=0; i<data.length; i++) {
            data[i] -= mean;
        } // end of for (int i=0; i<data.length; i++)
    }

    public static short[] removeMean(short[] data) {
        short[] out = new short[data.length];
        System.arraycopy(data, 0, out, 0, data.length);
        removeMeanInPlace(out);
        return out;
    }

    public static void removeMeanInPlace(short[] data) {
        Statistics stat = new Statistics(data);
        short mean = (short)Math.round((float)stat.mean());
        for (int i=0; i<data.length; i++) {
            data[i] -= mean;
        } // end of for (int i=0; i<data.length; i++)
    }


    private static final Logger logger = LoggerFactory.getLogger(RMean.class);

}// RMean
