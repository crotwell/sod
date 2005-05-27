package edu.sc.seis.sod.subsetter.origin;

import org.w3c.dom.Element;
import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfEvent.EventAttr;
import edu.iris.Fissures.IfEvent.Origin;
import edu.iris.Fissures.model.MicroSecondDate;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.subsetter.TimeRange;

public class OriginTimeRange extends TimeRange implements OriginSubsetter {

    public OriginTimeRange(Element config) throws ConfigurationException {
        super(config);
    }

    public boolean accept(EventAccessOperations event,
                          EventAttr eventAttr,
                          Origin origin) {
        return getMSTR().intersects(new MicroSecondDate(origin.origin_time));
    }
}// EventTimeRange
