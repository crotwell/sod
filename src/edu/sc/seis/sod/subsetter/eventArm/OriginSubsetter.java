package edu.sc.seis.sod.subsetter.eventArm;
import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfEvent.EventAttr;
import edu.iris.Fissures.IfEvent.Origin;
import edu.sc.seis.sod.subsetter.Subsetter;

/**
 * OriginSubsetter.java
 *
 *
 * Created: Tue Apr  2 13:32:13 2002
 *
 * @author <a href="mailto:telukutl@piglet">Srinivasa Telukutla</a>
 * @version
 */

public interface OriginSubsetter extends Subsetter{
    public boolean accept(EventAccessOperations eventAccess, EventAttr eventAttr, Origin preferred_origin)
        throws Exception;
}// OriginSubsetter
