package edu.sc.seis.sod.subsetter.eventArm;

import edu.sc.seis.sod.*;

import java.util.*;
import java.lang.*;
import org.w3c.dom.*;
import org.apache.log4j.*;
import edu.iris.Fissures.IfEvent.*;
import edu.iris.Fissures.event.*;
import edu.iris.Fissures.*;

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

public abstract class FlinnEngdahlRegion implements EventAttrSubsetter {
    /**
     * Creates a new <code>FlinnEngdahlRegion</code> instance.
     *
     * @param config an <code>Element</code> value
     */
    public FlinnEngdahlRegion (Element config){

        String regionStr = SodUtil.getNestedText(config);
        StringTokenizer strtok = new StringTokenizer(regionStr, " ");
        ArrayList arrayList = new ArrayList();
        while(strtok.hasMoreTokens()) {
            Integer newEntry = new Integer(Integer.parseInt(strtok.nextToken()));
            arrayList.add(newEntry);
        }
        regions = new Integer[arrayList.size()];
        regions = (Integer[]) arrayList.toArray(regions);

    }

    public boolean accept(EventAttr e) {
        if(e.region.type.value() == getType().value()) {
            for(int counter = 0; counter < regions.length; counter++) {
                if(e.region.number == regions[counter].intValue()) {
                    return true;
                }
            }
        }
        return false;
    }

    public abstract FlinnEngdahlType getType();

    private Integer[] regions = new Integer[0];

    static Category logger =
        Category.getInstance(FlinnEngdahlRegion.class.getName());
}// FlinnEngdahlRegion
