package edu.sc.seis.sod.subsetter.origin;

import edu.iris.Fissures.IfEvent.EventAttr;
import edu.iris.Fissures.IfEvent.Origin;
import edu.sc.seis.fissuresUtil.cache.CacheEvent;
import edu.sc.seis.sod.status.StringTree;
import edu.sc.seis.sod.status.StringTreeLeaf;

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

    public StringTree accept(CacheEvent event,
                             EventAttr eventAttr,
                             Origin e) {
        return new StringTreeLeaf(this, true);
    }
}// PassOrigin
