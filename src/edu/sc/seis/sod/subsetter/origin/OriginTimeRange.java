package edu.sc.seis.sod.subsetter.origin;

import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfEvent.EventAttr;
import edu.iris.Fissures.IfEvent.Origin;
import edu.iris.Fissures.model.MicroSecondDate;
import edu.sc.seis.sod.ConfigurationException;
import org.w3c.dom.Element;

public class OriginTimeRange extends edu.sc.seis.sod.subsetter.TimeRange
        implements OriginSubsetter {

    public OriginTimeRange(Element config) throws ConfigurationException {
        super(config);
    }

    public boolean accept(EventAccessOperations event,
                          EventAttr eventAttr,
                          Origin origin) {
        MicroSecondDate actualDate = new MicroSecondDate(origin.origin_time);
        if(getMSTR().intersects(actualDate))
            return true;
        else
            return false;
    }
}// EventTimeRange
