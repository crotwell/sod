package edu.sc.seis.sod.subsetter.eventArm;

import edu.sc.seis.sod.*;
import java.util.*;
import org.w3c.dom.*;
import edu.iris.Fissures.IfEvent.*;
import edu.iris.Fissures.event.*;
import edu.iris.Fissures.*;
/**
 * This subsetter is used to specify a sequence of OriginANDSubsetters. This subsetter is accepted only when all the
 * subsetters forming the sequence are accepted.
 *<pre>
 *  &lt;originAND&gt;
 *  &lt;description&gt;take any global 6.5 or better EQ&lt;/description&gt;
 *      &lt;magnitudeRange&gt;
 *          &lt;magType&gt;mb&lt;/magType&gt;
 *          &lt;min&gt;4.5&lt;/min&gt;
 *              &lt;/magnitudeRange&gt;
 *      &lt;eventArea&gt;
 *                  &lt;globalArea/&gt;
 *               &lt;/eventArea&gt;
 *  &lt;/originAND&gt;
 *
 *                           (or)
 *
 *   &lt;originAND&gt;
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
 *     &lt;/originAND&gt;
 *
 *                          (or)
 *
 *      &lt;originAND&gt;
 *               &lt;originAND&gt;
 *                       &lt;catalog&gt;&lt;value&gt;BIGQUAKE&lt;/value&gt;&lt;/catalog&gt;
 *                       &lt;catalog&gt;&lt;value&gt;BIGQUAKE&lt;/value&gt;&lt;/catalog&gt;
 *                       &lt;catalog&gt;&lt;value&gt;BIGQUAKE&lt;/value&gt;&lt;/catalog&gt;
 *               &lt;/originAND&gt;
 *               &lt;originArrayAND&gt;
 *                       &lt;originNOT&gt;
 *                               &lt;magnitudeRange&gt;
 *                                       &lt;magType&gt;mb&lt;/magType&gt;
 *                                       &lt;min&gt;7&lt;/min&gt;
 *                                       &lt;max&gt;10&lt;/max&gt;
 *                               &lt;/magnitudeRange&gt;
 *                       &lt;/originNOT&gt;
 *                &lt;/originArrayAND&gt;
 *
 *     &lt;/originAND&gt;
 *</pre>
 */

public final class OriginAND
    extends EventLogicalSubsetter
    implements OriginSubsetter {

    /**
     * Creates a new <code>OriginAND</code> instance.
     *
     * @param config an <code>Element</code> value
     * @exception ConfigurationException if an error occurs
     */
    public OriginAND (Element config) throws ConfigurationException {
    super(config);
    }

    /**
     * Describe <code>accept</code> method here.
     *
     * @param event an <code>EventAccessOperations</code> value
     * @param e an <code>Origin</code> value
     * @param cookies a <code>CookieJar</code> value
     * @return a <code>boolean</code> value
     * @exception Exception if an error occurs
     */
    public boolean accept(EventAccessOperations event, Origin e,  CookieJar cookies) throws Exception{
    Iterator it = filterList.iterator();
    while (it.hasNext()) {
        OriginSubsetter filter = (OriginSubsetter)it.next();
        if ( !filter.accept(event, e, cookies)) {
        return false;
        }
    }
    return true;
    }

}// OriginAND
