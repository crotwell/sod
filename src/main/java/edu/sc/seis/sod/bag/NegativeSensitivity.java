package edu.sc.seis.sod.bag;

import edu.sc.seis.seisFile.fdsnws.stationxml.Channel;
import edu.sc.seis.seisFile.fdsnws.stationxml.InstrumentSensitivity;
import edu.sc.seis.sod.model.common.FissuresException;
import edu.sc.seis.sod.model.common.QuantityImpl;
import edu.sc.seis.sod.model.seismogram.LocalSeismogramImpl;

public class NegativeSensitivity {

    public static boolean check(InstrumentSensitivity sensitivity) {
        return sensitivity.getSensitivityValue() < 0;
    }
    
    public static boolean check(QuantityImpl sensitivity) {
        return sensitivity.getValue() < 0;
    }

	public static ChannelSeismogram correct(Channel chan,
			LocalSeismogramImpl seis, InstrumentSensitivity sensitivity)
			throws FissuresException {
		if (check(sensitivity)) {
			return new ChannelSeismogram(chan, Arithmatic.mul(seis, -1),
					new InstrumentSensitivity(-1 * sensitivity.getSensitivityValue(),
							sensitivity.getFrequency()));
		}
		return new ChannelSeismogram(chan, seis, sensitivity);
	}
}
