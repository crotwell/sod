package edu.sc.seis.sod.subsetter.eventArm;

import edu.sc.seis.sod.*;
import java.util.*;
import org.w3c.dom.*;
import edu.iris.Fissures.IfEvent.*;
import edu.iris.Fissures.event.*;
import edu.iris.Fissures.*;
/**
 * Contains a single OriginSubsetter. OriginArrayOR returns true when the contained originSubsetter is
 * true for any one of the origins.
 *<pre>
 *  &lt;originArrayOR&gt;
 *        &lt;originNOT&gt;
 *               &lt;magnitudeRange&gt;
 *               &lt;magType&gt;mb&lt;/magType&gt;
 *               &lt;min&gt;7&lt;/min&gt;
 *               &lt;max&gt;10&lt;/max&gt;
 *               &lt;/magnitudeRange&gt;
 *        &lt;/originNOT&gt;
 *  &lt;/originArrayOR&gt;
 *</pre>
 */

public class OriginArrayOR  extends EventLogicalSubsetter
    implements OriginSubsetter {

    public OriginArrayOR (Element config) throws ConfigurationException {
        super(config);
    }

    public boolean accept(EventAccessOperations event, Origin e) throws Exception{
        Iterator it = filterList.iterator();
        while (it.hasNext()) {
            OriginSubsetter filter = (OriginSubsetter)it.next();
            Origin[] origins = event.get_origins();
            for(int counter = 0; counter < origins.length; counter++) {
                if (filter.accept(event, origins[counter])) { return true; }
            }
        }
        return false;
    }
}// OriginArrayOR
