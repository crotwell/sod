package edu.sc.seis.sod.subsetter.eventArm;

import edu.sc.seis.sod.*;
import java.util.*;
import org.w3c.dom.*;
import edu.iris.Fissures.IfEvent.*;
import edu.iris.Fissures.event.*;
import edu.iris.Fissures.*;

/**
 * This subsetter is used to specify a sequence of EventAttrSubsetters. This subsetter is accepted when even one
 * of the subsetters forming the sequence is accepted. If all the subsetters in the sequence are not accepted then
 * the eventAttrOR is not accepted.
 *<pre>
 *  &lt;originOR&gt;
 *              &lt;originAND&gt;
 *      &lt;description&gt;take any global 6.5 or better EQ&lt;/description&gt;
 *      &lt;magnitudeRange&gt;
 *          &lt;magType&gt;mb&lt;/magType&gt;
 *          &lt;min&gt;4.5&lt;/min&gt;
 *      &lt;/magnitudeRange&gt;
 *      &lt;eventArea&gt;
 *                  &lt;globalArea/&gt;
 *                &lt;/eventArea&gt;
 *              &lt;/originAND&gt;
 *
 *              &lt;originAND&gt;
 *      &lt;description&gt;take any 4.5 or better EQ in the southeast&lt;/description&gt;
 *      &lt;eventArea&gt;
 *         &lt;boxArea&gt;
 *          &lt;latitudeRange&gt;
 *              &lt;min&gt;28&lt;/min&gt;
 *              &lt;max&gt;38&lt;/max&gt;
 *          &lt;/latitudeRange&gt;
 *          &lt;longitudeRange&gt;
 *              &lt;min&gt;-85&lt;/min&gt;
 *              &lt;max&gt;-75&lt;/max&gt;
 *          &lt;/longitudeRange&gt;
 *             &lt;/boxArea&gt;
 *      &lt;/eventArea&gt;
 *      &lt;magnitudeRange&gt;
 *          &lt;magType&gt;mb&lt;/magType&gt;
 *          &lt;min&gt;4.5&lt;/min&gt;
 *      &lt;/magnitudeRange&gt;
 *              &lt;/originAND&gt;
 *
 *              &lt;originAND&gt;
 *      &lt;description&gt;take any 3.5 or better EQ in SC roughly. Note that the magnitudeRange here is a bit redundant because we already have a min magnitude of 3.5 in the EventFinder above&lt;/description&gt;
 *      &lt;eventArea&gt;
 *         &lt;boxArea&gt;
 *          &lt;latitudeRange&gt;
 *              &lt;min&gt;32&lt;/min&gt;
 *              &lt;max&gt;35.5&lt;/max&gt;
 *          &lt;/latitudeRange&gt;
 *          &lt;longitudeRange&gt;
 *              &lt;min&gt;-83.5&lt;/min&gt;
 *              &lt;max&gt;-78.5&lt;/max&gt;
 *          &lt;/longitudeRange&gt;
 *             &lt;/boxArea&gt;
 *      &lt;/eventArea&gt;
 *      &lt;magnitudeRange&gt;
 *          &lt;magType&gt;mb&lt;/magType&gt;
 *          &lt;min&gt;3.5&lt;/min&gt;
 *      &lt;/magnitudeRange&gt;
 *              &lt;/originAND&gt;
 *  &lt;/originOR&gt;
 *</pre>
/**
 * OriginOR.java
 *
 *
 * Created: Thu Mar 14 14:02:33 2002
 *
 * @author <a href="mailto:">Philip Crotwell</a>
 * @version
 */

public final class OriginOR extends EventLogicalSubsetter
    implements OriginSubsetter {

    public OriginOR (Element config) throws ConfigurationException {
        super(config);
    }

    public boolean accept(EventAccessOperations event, Origin e) throws Exception{
        Iterator it = filterList.iterator();
        while (it.hasNext()) {
            OriginSubsetter filter = (OriginSubsetter)it.next();
            if (filter.accept(event, e)) { return true; }
        }
        return false;
    }
}// OriginOR
