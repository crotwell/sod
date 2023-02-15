package edu.sc.seis.sod.bag;



import edu.sc.seis.seisFile.fdsnws.stationxml.InstrumentSensitivity;
import edu.sc.seis.sod.model.common.FissuresException;
import edu.sc.seis.sod.model.common.UnitImpl;
import edu.sc.seis.sod.model.common.UnknownUnit;
import edu.sc.seis.sod.model.seismogram.LocalSeismogramImpl;
import edu.sc.seis.sod.model.station.ChannelIdUtil;
import edu.sc.seis.sod.util.convert.stationxml.StationXMLToFissures;

/**
 * Applies the overall sensitivity to a seismogram. This is purely a scale
 * factor, no frequency change is done.
 */
public class ResponseGain {

    /**
     * Applies the overall sensitivity of the response to the seismogram. This
     * will promote short or int based seismograms to float to avoid rounding
     * and overflow problems.
     * @throws UnknownUnit 
     */
    public static LocalSeismogramImpl apply(LocalSeismogramImpl seis,
                                            InstrumentSensitivity inst)
            throws FissuresException, UnknownUnit {
        if(!InstrumentSensitivity.isValid(inst)) {
            throw new IllegalArgumentException("Invalid instrumentation for "
                    + ChannelIdUtil.toString(seis.channel_id));
        }
        return apply(seis,
                     inst.getSensitivityValue(),
                     StationXMLToFissures.convertUnit(inst.getInputUnits()));
    }

    public static LocalSeismogramImpl apply(LocalSeismogramImpl seis,
                                            float sensitivity_factor,
                                            UnitImpl initialUnits)
            throws FissuresException {
        // Sensitivity is COUNTs per Ground Motion, so should divide in order to
        // convert COUNT seismogram into Ground Motion.
        LocalSeismogramImpl outSeis;
        // don't use int or short, promote to float
        if(seis.can_convert_to_float()) {
            float[] fSeries = seis.get_as_floats();
            float[] out = new float[fSeries.length];
            for(int i = 0; i < fSeries.length; i++) {
                out[i] = fSeries[i] / sensitivity_factor;
            }
            outSeis = new LocalSeismogramImpl(seis, out);
        } else {
            double[] dSeries = seis.get_as_doubles();
            double[] out = new double[dSeries.length];
            for(int i = 0; i < dSeries.length; i++) {
                out[i] = dSeries[i] / sensitivity_factor;
            }
            outSeis = new LocalSeismogramImpl(seis, out);
        } // end of else
        outSeis.y_unit = initialUnits;
        return outSeis;
    }
    
    
}// ResponseGain
