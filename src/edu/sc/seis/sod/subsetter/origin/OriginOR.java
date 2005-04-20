package edu.sc.seis.sod.subsetter.origin;

import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfEvent.EventAttr;
import edu.iris.Fissures.IfEvent.Origin;
import edu.sc.seis.sod.ConfigurationException;
import java.util.Iterator;
import org.w3c.dom.Element;

/**
 * OriginOR.java Created: Thu Mar 14 14:02:33 2002
 * 
 * @author <a href="mailto:">Philip Crotwell </a>
 * @version
 */
public final class OriginOR extends EventLogicalSubsetter implements
        OriginSubsetter {

    public OriginOR(Element config) throws ConfigurationException {
        super(config);
    }

    public boolean accept(EventAccessOperations event,
                          EventAttr eventAttr,
                          Origin e) throws Exception {
        Iterator it = filterList.iterator();
        while(it.hasNext()) {
            OriginSubsetter filter = (OriginSubsetter)it.next();
            if(filter.accept(event, eventAttr, e)) { return true; }
        }
        return false;
    }
}// OriginOR
