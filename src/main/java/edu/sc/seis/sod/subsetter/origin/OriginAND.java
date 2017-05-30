package edu.sc.seis.sod.subsetter.origin;

import java.util.Iterator;

import org.w3c.dom.Element;

import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.model.event.CacheEvent;
import edu.sc.seis.sod.model.event.EventAttrImpl;
import edu.sc.seis.sod.model.event.OriginImpl;
import edu.sc.seis.sod.status.ShortCircuit;
import edu.sc.seis.sod.status.StringTree;
import edu.sc.seis.sod.status.StringTreeBranch;
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

public final class OriginAND extends EventLogicalSubsetter
    implements OriginSubsetter {

    public OriginAND (Element config) throws ConfigurationException {
        super(config);
    }

    public StringTree accept(CacheEvent event, EventAttrImpl eventAttr, OriginImpl e) throws Exception{
        Iterator it = filterList.iterator();
        StringTree[] result = new StringTree[filterList.size()];
        int i=0;
        while (it.hasNext()) {
            OriginSubsetter filter = (OriginSubsetter)it.next();
            result[i] = filter.accept(event, eventAttr, e);
            if ( ! result[i].isSuccess()) { 
                for(int j = i + 1; j < result.length; j++) {
                    result[j] = new ShortCircuit(filterList.get(j));
                }
                return new StringTreeBranch(this, false, result);
            }
            i++;
        }
        return new StringTreeBranch(this, true, result);
    }
}// OriginAND
