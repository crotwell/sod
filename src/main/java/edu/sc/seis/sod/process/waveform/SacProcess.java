package edu.sc.seis.sod.process.waveform;

import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfNetwork.Channel;
import edu.sc.seis.seisFile.sac.SacTimeSeries;

interface SacProcess {

    public void process(SacTimeSeries sac,
                        EventAccessOperations event,
                        Channel channel);
}