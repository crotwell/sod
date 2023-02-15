package edu.sc.seis.sod.bag;

import edu.sc.seis.seisFile.fdsnws.stationxml.Channel;
import edu.sc.seis.seisFile.fdsnws.stationxml.InstrumentSensitivity;
import edu.sc.seis.sod.model.seismogram.LocalSeismogramImpl;

public class ChannelSeismogram {

	public ChannelSeismogram(Channel chan, LocalSeismogramImpl seis, InstrumentSensitivity sensitivity) {
		this.seis = seis;
		this.chan = chan;
		this.sensitivity = sensitivity;
	}

	public Channel getChannel() {
		return chan;
	}

	public LocalSeismogramImpl getSeismogram() {
		return seis;
	}

	public InstrumentSensitivity getSensitivity() {
        if(sensitivity == null){
            throw new UnsupportedOperationException("This channelseismogram has no sensitivity");
        }
		return sensitivity;
	}

	LocalSeismogramImpl seis;

	Channel chan;

	InstrumentSensitivity sensitivity;
}
