package edu.sc.seis.sod.subsetter.origin;

import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfEvent.EventAttr;
import edu.iris.Fissures.IfEvent.Origin;
import edu.sc.seis.sod.ConfigurationException;
import java.util.Iterator;
import org.w3c.dom.Element;

/**
 * This subsetter is used to specify a negation of OriginSubsetter. This subsetter is accepted only when the included
 * subsetter is false.
 *<pre>
 *  &lt;originNOT&gt;
 *         &lt;magnitudeRange&gt;
 *                 &lt;magType&gt;mb&lt;/magType&gt;
 *                  &lt;min&gt;7&lt;/min&gt;
 *                  &lt;max&gt;10&lt;/max&gt;
 *           &lt;/magnitudeRange&gt;
 *    &lt;/originNOT&gt;
 *</pre>
 */

public final class OriginNOT extends EventLogicalSubsetter
    implements OriginSubsetter {

    public OriginNOT (Element config) throws ConfigurationException {
        super(config);
    }

    public boolean accept(EventAccessOperations event, EventAttr eventAttr, Origin e) throws Exception{
        Iterator it = filterList.iterator();
        if (it.hasNext()) {
            OriginSubsetter filter = (OriginSubsetter)it.next();
            if (filter.accept(event, eventAttr, e)) { return false; }
        }
        return true;
    }
}// OriginNOT
