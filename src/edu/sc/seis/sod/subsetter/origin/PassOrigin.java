package edu.sc.seis.sod.subsetter.origin;

import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfEvent.EventAttr;
import edu.iris.Fissures.IfEvent.Origin;
import org.w3c.dom.Element;

/**
 * PassOrigin.java
 *
 *
 * Created: Thu Mar 14 14:02:33 2002
 *
 * @author <a href="mailto:">Philip Crotwell</a>
 * @version
 */

public class PassOrigin implements OriginSubsetter {
    public PassOrigin() {}

    public PassOrigin(Element config) { }

    public boolean accept(EventAccessOperations event, EventAttr eventAttr, Origin e) throws Exception{
        return true;
    }
}// PassOrigin
