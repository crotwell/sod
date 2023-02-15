package edu.sc.seis.sod.bag;

import edu.sc.seis.seisFile.fdsnws.stationxml.Channel;
import edu.sc.seis.seisFile.fdsnws.stationxml.InstrumentSensitivity;
import edu.sc.seis.sod.model.common.FissuresException;
import edu.sc.seis.sod.model.seismogram.LocalSeismogramImpl;

public class PolarityCheck {

	public static boolean check(Channel chan, LocalSeismogramImpl seis,
	                            InstrumentSensitivity sensitivity) throws FissuresException {
		return FlippedChannel.check(chan)
				&& NegativeSensitivity.check(sensitivity);
	}

	public static ChannelSeismogram correct(Channel chan,
			LocalSeismogramImpl seis, InstrumentSensitivity sensitivity)
			throws FissuresException {
		if (NegativeSensitivity.check(sensitivity)
				&& FlippedChannel.check(chan)) {
			// both sensitivity and channel are flipped so seis is same
			return new ChannelSeismogram(OrientationUtil.flip(chan), seis,
					new InstrumentSensitivity(-1 * sensitivity.getSensitivityValue(),
							sensitivity.getFrequency()));
		} else if (NegativeSensitivity.check(sensitivity)) {
			return NegativeSensitivity.correct(chan, seis, sensitivity);
		} else if (FlippedChannel.check(chan)) {
			return FlippedChannel.correct(chan, seis, sensitivity);
		}

		return new ChannelSeismogram(chan, seis, sensitivity);
	}
}
