package edu.sc.seis.sod.subsetter.origin;

import edu.iris.Fissures.event.EventAttrImpl;
import edu.iris.Fissures.event.OriginImpl;
import edu.sc.seis.fissuresUtil.cache.CacheEvent;
import edu.sc.seis.sod.status.StringTree;
import edu.sc.seis.sod.status.StringTreeLeaf;

/**
 * PassOrigin.java
 * 
 * 
 * Created: Thu Mar 14 14:02:33 2002
 * 
 * @author Philip Crotwell
 */
public class PassOrigin implements OriginSubsetter {

    public StringTree accept(CacheEvent event,
                             EventAttrImpl eventAttr,
                             OriginImpl e) {
        return new StringTreeLeaf(this, true);
    }
}// PassOrigin
