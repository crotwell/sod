package edu.sc.seis.sod.subsetter.station;

import java.util.Iterator;
import org.w3c.dom.Element;
import edu.iris.Fissures.IfNetwork.Station;
import edu.sc.seis.sod.ConfigurationException;

public final class StationAND extends StationLogicalSubsetter implements
        StationSubsetter {

    public StationAND(Element config) throws ConfigurationException {
        super(config);
    }

    public boolean accept(Station e) throws Exception {
        Iterator it = filterList.iterator();
        while(it.hasNext()) {
            StationSubsetter filter = (StationSubsetter)it.next();
            if(!filter.accept(e)) { return false; }
        }
        return true;
    }
}// StationAND
