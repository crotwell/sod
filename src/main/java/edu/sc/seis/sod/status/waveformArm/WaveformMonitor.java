package edu.sc.seis.sod.status.waveformArm;

import edu.sc.seis.sod.SodElement;
import edu.sc.seis.sod.hibernate.eventpair.EventChannelPair;
import edu.sc.seis.sod.hibernate.eventpair.EventNetworkPair;
import edu.sc.seis.sod.hibernate.eventpair.EventStationPair;
import edu.sc.seis.sod.hibernate.eventpair.EventVectorPair;

public interface WaveformMonitor extends SodElement {

    public void update(EventNetworkPair ecp);

    public void update(EventStationPair ecp);
    
    public void update(EventChannelPair ecp);
    
    public void update(EventVectorPair evp);
}