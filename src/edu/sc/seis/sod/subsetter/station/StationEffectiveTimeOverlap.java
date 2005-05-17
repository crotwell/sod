package edu.sc.seis.sod.subsetter.station;

import org.apache.log4j.Category;
import org.w3c.dom.Element;
import edu.iris.Fissures.TimeRange;
import edu.iris.Fissures.IfNetwork.Station;
import edu.sc.seis.sod.ConfigurationException;
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

    public boolean accept(Station station) {
        return overlaps(station.effective_time);
    }

    static Category logger = Category.getInstance(StationEffectiveTimeOverlap.class.getName());
}// StationEffectiveTimeOverlap
