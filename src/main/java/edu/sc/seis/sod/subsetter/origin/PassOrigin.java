package edu.sc.seis.sod.subsetter.origin;

import edu.sc.seis.sod.model.event.CacheEvent;
import edu.sc.seis.sod.model.event.EventAttrImpl;
import edu.sc.seis.sod.model.event.OriginImpl;
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
