package edu.sc.seis.sod.subsetter.eventArm;
import edu.iris.Fissures.IfEvent.EventAttr;
import edu.sc.seis.sod.subsetter.Subsetter;

/**
 * EventAttrSubsetter.java
 *
 * Created: Thu Dec 13 17:03:44 2001
 *
 * @author <a href="mailto:">Philip Crotwell</a>
 * @version
 */

public interface EventAttrSubsetter extends Subsetter {

    public boolean accept(EventAttr event) throws Exception;


}// EventSubsetter
