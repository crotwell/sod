package edu.sc.seis.sod.subsetter.origin;

import java.util.Iterator;
import org.w3c.dom.Element;
import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfEvent.EventAttr;
import edu.iris.Fissures.IfEvent.Origin;
import edu.sc.seis.sod.ConfigurationException;

/**
 * Contains a single OriginSubsetter. OriginArrayAND returns true when the
 * contained originSubsetter is true for all the origins.
 */
public class OriginArrayAND extends EventLogicalSubsetter implements
        OriginSubsetter {

    public OriginArrayAND(Element config) throws ConfigurationException {
        super(config);
    }

    public boolean accept(EventAccessOperations event,
                          EventAttr eventAttr,
                          Origin e) throws Exception {
        Iterator it = filterList.iterator();
        while(it.hasNext()) {
            OriginSubsetter filter = (OriginSubsetter)it.next();
            Origin[] origins = event.get_origins();
            for(int counter = 0; counter < origins.length; counter++) {
                if(!filter.accept(event, eventAttr, origins[counter])) { return false; }
            }
        }
        return true;
    }
}// OriginArrayAND
