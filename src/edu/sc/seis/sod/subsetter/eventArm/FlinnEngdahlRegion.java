package edu.sc.seis.sod.subsetter.eventArm;

import edu.iris.Fissures.FlinnEngdahlType;
import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfEvent.EventAttr;
import edu.iris.Fissures.IfEvent.Origin;
import edu.sc.seis.sod.SodUtil;
import java.util.ArrayList;
import java.util.StringTokenizer;
import org.apache.log4j.Category;
import org.w3c.dom.Element;

/**
 * FlinnEngdahlRegion.java
 *
 * FlinnEngdahlRegion can be either GeorgraphicRegion or SeismicRegion
 *
 *
 * Created: Tue Mar 19 13:27:02 2002
 *
 * @author <a href="mailto:">Philip Crotwell</a>
 * @version
 */

public abstract class FlinnEngdahlRegion implements OriginSubsetter {
    public FlinnEngdahlRegion (Element config){
        region = Integer.parseInt(SodUtil.getNestedText(config));
    }

    public boolean accept(EventAccessOperations eventAccess, EventAttr eventAttr, Origin preferred_origin) {
        if(eventAttr.region.type.value() == getType().value()) {
            if(eventAttr.region.number == region) {
                return true;
            }
        }
        return false;
    }

    public abstract FlinnEngdahlType getType();

    private int region;

    static Category logger =
        Category.getInstance(FlinnEngdahlRegion.class.getName());
}// FlinnEngdahlRegion
