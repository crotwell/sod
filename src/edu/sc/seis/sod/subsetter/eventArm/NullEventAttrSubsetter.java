package edu.sc.seis.sod.subsetter.eventArm;

import edu.iris.Fissures.IfEvent.EventAttr;

/**
 * NullEventAttrSubsetter.java
 *
 *
 * Created: Thu Mar 14 14:02:33 2002
 *
 * @author <a href="mailto:">Philip Crotwell</a>
 * @version
 */

public class NullEventAttrSubsetter implements EventAttrSubsetter {
    public NullEventAttrSubsetter () {}

    public boolean accept(EventAttr e) { return true; }
}// NullEventAttrSubsetter
