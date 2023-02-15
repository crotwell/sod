package edu.sc.seis.sod.util.convert.stationxml;

import edu.sc.seis.seisFile.fdsnws.stationxml.Channel;
import edu.sc.seis.sod.model.common.QuantityImpl;

public class ChannelSensitivityBundle {
    
    public ChannelSensitivityBundle(Channel chan, QuantityImpl sensitivity) {
        super();
        this.chan = chan;
        this.sensitivity = sensitivity;
    }

    public Channel getChan() {
        return chan;
    }
    
    public QuantityImpl getSensitivity() {
        return sensitivity;
    }
    
    private Channel chan;
    private QuantityImpl sensitivity;
}
