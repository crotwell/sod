package edu.sc.seis.sod.subsetter.eventArm;

import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfEvent.EventAttr;
import edu.iris.Fissures.IfEvent.Origin;

/**
 * NullEventAttrSubsetter.java
 *
 *
 * Created: Thu Mar 14 14:02:33 2002
 *
 * @author <a href="mailto:">Philip Crotwell</a>
 * @version
 */

public class NullEventAttrSubsetter implements OriginSubsetter {
    public NullEventAttrSubsetter () {}

    public boolean accept(EventAccessOperations eventAccess, EventAttr eventAttr, Origin preferred_origin) { return true; }
}// NullEventAttrSubsetter
