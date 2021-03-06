package edu.sc.seis.sod.subsetter.origin;

import org.w3c.dom.Element;

import edu.iris.Fissures.event.EventAttrImpl;
import edu.iris.Fissures.event.OriginImpl;
import edu.sc.seis.fissuresUtil.cache.CacheEvent;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.SodElement;
import edu.sc.seis.sod.status.StringTree;
import edu.sc.seis.sod.status.StringTreeLeaf;
import edu.sc.seis.sod.subsetter.AreaSubsetter;

/**
 * This class is used to
 *         represent the subsetter EventArea. Event Area implements
 *         EventSubsetter and can be any one of GlobalArea or BoxArea or
 *         PointDistanceArea or FlinneEngdahlArea.
 *         
 * Created: Thu Mar 14 14:02:33 2002
 * 
 * @author Philip Crotwell 
 */
public class EventArea extends AreaSubsetter implements OriginSubsetter,
        SodElement {

    public EventArea(Element config) throws ConfigurationException {
        super(config);
    }

    /**
     * returns true if the given origin is within the area specified in the
     * configuration file else returns false.
     */
    public StringTree accept(CacheEvent event,
                          EventAttrImpl eventAttr,
                          OriginImpl e) throws Exception {
        return new StringTreeLeaf(this, super.accept(e.getLocation()));
    }
}
