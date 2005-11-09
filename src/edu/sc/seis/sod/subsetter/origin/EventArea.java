package edu.sc.seis.sod.subsetter.origin;

import org.w3c.dom.Element;
import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfEvent.EventAttr;
import edu.iris.Fissures.IfEvent.Origin;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.SodElement;
import edu.sc.seis.sod.subsetter.AreaSubsetter;

/**
 * Created: Thu Mar 14 14:02:33 2002
 * 
 * @author <a href="mailto:">Philip Crotwell </a> This class is used to
 *         represent the subsetter EventArea. Event Area implements
 *         EventSubsetter and can be any one of GlobalArea or BoxArea or
 *         PointDistanceArea or FlinneEngdahlArea.
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
    public boolean accept(EventAccessOperations event,
                          EventAttr eventAttr,
                          Origin e) throws Exception {
        return super.accept(e.my_location);
    }
}
