package edu.sc.seis.sod.subsetter.eventArm;

import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfEvent.EventAttr;
import edu.iris.Fissures.IfEvent.Origin;
import org.w3c.dom.Element;

/**
 * NullOriginSubsetter.java
 *
 *
 * Created: Thu Mar 14 14:02:33 2002
 *
 * @author <a href="mailto:">Philip Crotwell</a>
 * @version
 */

public class NullOriginSubsetter implements OriginSubsetter {
    public NullOriginSubsetter () {}

    public NullOriginSubsetter (Element config) { }

    public boolean accept(EventAccessOperations event, EventAttr eventAttr, Origin e) throws Exception{
        return true;
    }
}// NullOriginSubsetter
