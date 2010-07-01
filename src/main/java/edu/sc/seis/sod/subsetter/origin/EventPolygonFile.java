package edu.sc.seis.sod.subsetter.origin;

import org.w3c.dom.Element;

import edu.iris.Fissures.Location;
import edu.iris.Fissures.event.EventAttrImpl;
import edu.iris.Fissures.event.OriginImpl;
import edu.sc.seis.fissuresUtil.bag.AreaUtil;
import edu.sc.seis.fissuresUtil.cache.CacheEvent;
import edu.sc.seis.fissuresUtil.display.configuration.DOMHelper;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.status.StringTree;
import edu.sc.seis.sod.status.StringTreeLeaf;
import edu.sc.seis.sod.subsetter.AreaSubsetter;

public class EventPolygonFile implements OriginSubsetter {

    public EventPolygonFile(Element el) throws ConfigurationException {
        locations = AreaSubsetter.extractPolygon(DOMHelper.extractText(el, "."));
    }

    public StringTree accept(CacheEvent eventAccess,
                             EventAttrImpl eventAttr,
                             OriginImpl preferred_origin) {
        return new StringTreeLeaf(this,
                                  AreaUtil.inArea(locations,
                                                  preferred_origin.getLocation()));
    }

    private Location[] locations;
}
