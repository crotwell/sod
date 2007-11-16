package edu.sc.seis.sod.example;

import org.w3c.dom.Element;
import edu.iris.Fissures.IfEvent.EventAttr;
import edu.iris.Fissures.IfEvent.Origin;
import edu.sc.seis.fissuresUtil.cache.CacheEvent;
import edu.sc.seis.fissuresUtil.display.configuration.DOMHelper;
import edu.sc.seis.sod.status.Fail;
import edu.sc.seis.sod.status.Pass;
import edu.sc.seis.sod.status.StringTree;
import edu.sc.seis.sod.subsetter.origin.OriginSubsetter;

public class LatOMatic implements OriginSubsetter {

    public LatOMatic(Element el) {
        maxLat = DOMHelper.extractFloat(el, "maxLat", 0);
    }

    public StringTree accept(CacheEvent eventAccess,
                             EventAttr eventAttr,
                             Origin preferred_origin) throws Exception {
        if(preferred_origin.my_location.latitude > maxLat) {
            return new Fail(this, "origin not below " + maxLat + " latitude");
        }
        return new Pass(this);
    }

    float maxLat;
}