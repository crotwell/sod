package edu.sc.seis.sod.subsetter.origin;

import org.w3c.dom.Element;
import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfEvent.EventAttr;
import edu.iris.Fissures.IfEvent.Origin;
import edu.iris.Fissures.model.MicroSecondDate;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.status.StringTree;
import edu.sc.seis.sod.status.StringTreeLeaf;
import edu.sc.seis.sod.subsetter.TimeRange;

public class OriginTimeRange extends TimeRange implements OriginSubsetter {

    public OriginTimeRange(Element config) throws ConfigurationException {
        super(config);
    }

    public StringTree accept(EventAccessOperations event,
                          EventAttr eventAttr,
                          Origin origin) {
        return new StringTreeLeaf(this, getMSTR().intersects(new MicroSecondDate(origin.origin_time)));
    }
}// EventTimeRange
