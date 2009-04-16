package edu.sc.seis.sod.subsetter.station;

import org.apache.log4j.Category;
import org.w3c.dom.Element;

import edu.iris.Fissures.TimeRange;
import edu.iris.Fissures.IfNetwork.NetworkAccess;
import edu.iris.Fissures.IfNetwork.Station;
import edu.iris.Fissures.model.MicroSecondDate;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.status.StringTree;
import edu.sc.seis.sod.status.StringTreeLeaf;
import edu.sc.seis.sod.subsetter.EffectiveTimeOverlap;

public class StationEffectiveTimeOverlap extends EffectiveTimeOverlap implements
        StationSubsetter {

    public StationEffectiveTimeOverlap(Element config)
            throws ConfigurationException {
        super(config);
    }

    public StationEffectiveTimeOverlap(TimeRange tr) {
        super(tr);
    }
    
    public StationEffectiveTimeOverlap(MicroSecondDate start, MicroSecondDate end) {
        super(start, end);
    }

    public StringTree accept(Station station, NetworkAccess network) {
        return new StringTreeLeaf(this, overlaps(station));
    }
    
    public boolean overlaps(Station station) {
        return overlaps(station.getEffectiveTime());
    }

    static Category logger = Category.getInstance(StationEffectiveTimeOverlap.class.getName());
}// StationEffectiveTimeOverlap
