package edu.sc.seis.sod.status.waveformArm;

import edu.sc.seis.sod.EventChannelPair;
import edu.sc.seis.sod.EventNetworkPair;
import edu.sc.seis.sod.EventStationPair;
import edu.sc.seis.sod.EventVectorPair;
import edu.sc.seis.sod.SodElement;

public interface WaveformMonitor extends SodElement {

    public void update(EventNetworkPair ecp);

    public void update(EventStationPair ecp);
    
    public void update(EventChannelPair ecp);
    
    public void update(EventVectorPair evp);
}