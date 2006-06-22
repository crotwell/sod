package edu.sc.seis.sod.example;

import org.w3c.dom.Element;
import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfEvent.EventAttr;
import edu.iris.Fissures.IfEvent.Origin;
import edu.sc.seis.fissuresUtil.display.configuration.DOMHelper;
import edu.sc.seis.sod.status.StringTree;
import edu.sc.seis.sod.status.StringTreeLeaf;
import edu.sc.seis.sod.subsetter.origin.OriginSubsetter;


public class LatOMatic implements OriginSubsetter {
    
    public LatOMatic(Element el) {
        maxLat = DOMHelper.extractFloat(el, "maxLat", 0);
    }

    public StringTree accept(EventAccessOperations eventAccess,
                             EventAttr eventAttr,
                             Origin preferred_origin) throws Exception {
        if (preferred_origin.my_location.latitude > maxLat) {
            return new StringTreeLeaf(this, false, "origin not in the southern hemisphere");
        } else {
            return new StringTreeLeaf(this, true);
        }
    }
    
    float maxLat;
}
