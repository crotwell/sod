/**
 * Pad.java
 *
 * @author Created by Omnicore CodeGuide
 */

package edu.sc.seis.sod.bag;

import java.time.Duration;

import edu.sc.seis.seisFile.TimeUtils;
import edu.sc.seis.sod.model.common.FissuresException;
import edu.sc.seis.sod.model.common.SamplingImpl;
import edu.sc.seis.sod.model.common.UnitImpl;
import edu.sc.seis.sod.model.seismogram.LocalSeismogramImpl;

public class Pad implements LocalSeismogramFunction  {

    public Pad(Duration padTime) {
        this.timeInterval = padTime;
    }

    public Pad(int padPoints) {
        this.padPoints = padPoints;
    }

    /**
     * Method apply
     *
     * @param    seis                a  LocalSeismogramImpl
     *
     * @return   a LocalSeismogramImpl
     *
     * @exception   Exception
     *
     */
    public LocalSeismogramImpl apply(LocalSeismogramImpl seis) throws Exception {
        if (timeInterval != null) {
            return pad(seis, timeInterval);
        } else {
            return pad(seis, padPoints);
        }
    }


    public LocalSeismogramImpl pad(LocalSeismogramImpl seis, Duration padSize) throws FissuresException  {

        SamplingImpl samp = seis.sampling_info;
        double period = TimeUtils.durationToDoubleSeconds(samp.getPeriod());

        int padPoints = (int)(Math.ceil(TimeUtils.durationToDoubleSeconds(padSize) / period));
        return pad(seis, padPoints);
    }

    public LocalSeismogramImpl pad(LocalSeismogramImpl seis, int padPoints) throws FissuresException  {
        LocalSeismogramImpl outSeis;
        int newSize = seis.getNumPoints()+padPoints;
        if (seis.can_convert_to_short()) {
            short[] outS = new short[newSize];
            short[] inS = seis.get_as_shorts();
            System.arraycopy(inS, 0, outS, 0, inS.length);
            outSeis = new LocalSeismogramImpl(seis, outS);
        } else if (seis.can_convert_to_long()) {
            int[] outI = new int[newSize];
            int[] inI = seis.get_as_longs();
            System.arraycopy(inI, 0, outI, 0, inI.length);
            outSeis = new LocalSeismogramImpl(seis, outI);
        } else if (seis.can_convert_to_float()) {
            float[] outF = new float[newSize];
            float[] inF = seis.get_as_floats();
            System.arraycopy(inF, 0, outF, 0, inF.length);
            outSeis = new LocalSeismogramImpl(seis, outF);
        } else {
            double[] outD = new double[newSize];
            double[] inD = seis.get_as_doubles();
            System.arraycopy(inD, 0, outD, 0, inD.length);
            outSeis = new LocalSeismogramImpl(seis, outD);
        } // end of else
        return outSeis;
    }

    protected Duration timeInterval = null;

    protected int padPoints = 0;

}

