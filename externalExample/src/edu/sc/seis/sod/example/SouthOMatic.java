package edu.sc.seis.sod.example;

import edu.iris.Fissures.IfEvent.EventAttr;
import edu.iris.Fissures.IfEvent.Origin;
import edu.sc.seis.fissuresUtil.cache.CacheEvent;
import edu.sc.seis.sod.status.Fail;
import edu.sc.seis.sod.status.Pass;
import edu.sc.seis.sod.status.StringTree;
import edu.sc.seis.sod.subsetter.origin.OriginSubsetter;

public class SouthOMatic implements OriginSubsetter {

    public StringTree accept(CacheEvent eventAccess,
                             EventAttr eventAttr,
                             Origin preferred_origin) {
        if(preferred_origin.getLocation().latitude > 0) {
            return new Fail(this, "origin not in the southern hemisphere");
        }
        return new Pass(this);
    }
}