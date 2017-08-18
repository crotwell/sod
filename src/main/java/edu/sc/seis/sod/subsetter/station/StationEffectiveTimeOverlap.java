package edu.sc.seis.sod.subsetter.station;

import org.w3c.dom.Element;

import edu.sc.seis.seisFile.fdsnws.stationxml.Station;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.model.common.MicroSecondDate;
import edu.sc.seis.sod.model.common.MicroSecondTimeRange;
import edu.sc.seis.sod.source.event.MicroSecondTimeRangeSupplier;
import edu.sc.seis.sod.source.network.NetworkSource;
import edu.sc.seis.sod.status.StringTree;
import edu.sc.seis.sod.status.StringTreeLeaf;
import edu.sc.seis.sod.subsetter.EffectiveTimeOverlap;

public class StationEffectiveTimeOverlap extends EffectiveTimeOverlap implements
        StationSubsetter {

    public StationEffectiveTimeOverlap(MicroSecondTimeRangeSupplier timeRange) {
        super(timeRange);
    }
    
    public StationEffectiveTimeOverlap(Element config)
            throws ConfigurationException {
        super(config);
    }

    public StationEffectiveTimeOverlap(MicroSecondTimeRange tr) {
        super(tr);
    }
    
    public StationEffectiveTimeOverlap(MicroSecondDate start, MicroSecondDate end) {
        super(start, end);
    }

    public StringTree accept(Station station, NetworkSource network) {
        return new StringTreeLeaf(this, overlaps(station));
    }
    
    public boolean overlaps(Station station) {
        return overlaps(station.getEffectiveTime());
    }

    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(StationEffectiveTimeOverlap.class);
    
}// StationEffectiveTimeOverlap
