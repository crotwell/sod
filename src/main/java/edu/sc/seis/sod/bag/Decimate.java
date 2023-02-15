package edu.sc.seis.sod.bag;

import edu.sc.seis.sod.model.common.SamplingImpl;
import edu.sc.seis.sod.model.seismogram.LocalSeismogramImpl;
import edu.sc.seis.sod.model.seismogram.TimeSeriesDataSel;

public class Decimate implements LocalSeismogramFunction {

    public Decimate(int factor) {
        this.factor = factor;
    }

    public LocalSeismogramImpl apply(LocalSeismogramImpl seis) throws Exception {
        LocalSeismogramImpl outSeis;
        TimeSeriesDataSel outData = new TimeSeriesDataSel();
        int numPts = (int)Math.ceil(1.0f*seis.num_points / factor);
        if(seis.can_convert_to_short()) {
            short[] inS = seis.get_as_shorts();
            short[] outS = new short[numPts];
            for(int i = 0; i < outS.length; i++) {
                outS[i] = inS[i * factor];
            }
            outData.sht_values(outS);
        } else if(seis.can_convert_to_long()) {
            int[] outI = new int[numPts];
            int[] inI = seis.get_as_longs();
            for(int i = 0; i < outI.length; i++) {
                outI[i] = inI[i * factor];
            }
            outData.int_values(outI);
        } else if(seis.can_convert_to_float()) {
            float[] outF = new float[numPts];
            float[] inF = seis.get_as_floats();
            for(int i = 0; i < outF.length; i++) {
                outF[i] = inF[i * factor];
            }
            outData.flt_values(outF);
        } else {
            double[] outD = new double[numPts];
            double[] inD = seis.get_as_doubles();
            for(int i = 0; i < outD.length; i++) {
                outD[i] = inD[i * factor];
            }
            outData.dbl_values(outD);
        } // end of else
        outSeis = new LocalSeismogramImpl(seis.get_id(),
                                          seis.properties,
                                          seis.begin_time,
                                          numPts,
                                          new SamplingImpl(seis.getSampling().getNumPoints(),
                                                           seis.getSampling().getTimeInterval().multipliedBy(factor)),
                                          seis.y_unit,
                                          seis.channel_id,
                                          seis.parm_ids,
                                          outData);
        return outSeis;
    }

    private int factor;

    public int getFactor() {
        return factor;
    }
}
