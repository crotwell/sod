package edu.sc.seis.sod.status.waveformArm;

import edu.sc.seis.sod.EventChannelPair;
import edu.sc.seis.sod.SodElement;

public interface WaveformMonitor extends SodElement {

    public void update(EventChannelPair ecp);
}