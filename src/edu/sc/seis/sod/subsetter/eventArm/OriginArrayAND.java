package edu.sc.seis.sod.subsetter.eventArm;

import edu.sc.seis.sod.*;
import java.util.*;
import org.w3c.dom.*;
import edu.iris.Fissures.IfEvent.*;
import edu.iris.Fissures.event.*;
import edu.iris.Fissures.*;

/**
 * Contains a single OriginSubsetter. OriginArrayAND returns true when the contained originSubsetter is
 * true for all the origins.
 *<pre>
 *  &lt;originArrayAND&gt;
 *        &lt;originNOT&gt;
 *               &lt;magnitudeRange&gt;
 *               &lt;magType&gt;mb&lt;/magType&gt;
 *               &lt;min&gt;7&lt;/min&gt;
 *               &lt;max&gt;10&lt;/max&gt;
 *               &lt;/magnitudeRange&gt;
 *        &lt;/originNOT&gt;
 *  &lt;/originArrayAND&gt;
 *</pre>
 */

public class OriginArrayAND  extends EventLogicalSubsetter
    implements OriginSubsetter {
    public OriginArrayAND (Element config) throws ConfigurationException {
        super(config);
    }

    public boolean accept(EventAccessOperations event, EventAttr eventAttr, Origin e) throws Exception{
        Iterator it = filterList.iterator();
        while (it.hasNext()) {
            OriginSubsetter filter = (OriginSubsetter)it.next();
            Origin[] origins = event.get_origins();
            for(int counter = 0; counter < origins.length; counter++) {
                if (!filter.accept(event, eventAttr, origins[counter])) { return false; }
            }
        }
        return true;
    }
}// OriginArrayAND
