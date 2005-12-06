package edu.sc.seis.sod.subsetter.origin;

import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfEvent.EventAttr;
import edu.iris.Fissures.IfEvent.Origin;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.status.StringTree;
import edu.sc.seis.sod.status.StringTreeBranch;
import org.w3c.dom.Element;

/**
 * originXOR contains a sequence of originSubsetters. The minimum value of the sequence is 2 and
 * the max value of the sequence is 2.
 *<pre>
 *      &lt;originXOR&gt;
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
 *      &lt;/originNOT&gt;
 *</pre>
 */

/**
 * OriginXOR.java
 *
 *
 * Created: Thu Mar 14 14:02:33 2002
 *
 * @author <a href="mailto:">Philip Crotwell</a>
 * @version
 */

public final class OriginXOR extends EventLogicalSubsetter
    implements OriginSubsetter {

    public OriginXOR (Element config) throws ConfigurationException {
        super(config);
    }

    public StringTree accept(EventAccessOperations event, EventAttr eventAttr, Origin e) throws Exception{
        OriginSubsetter filterA = (OriginSubsetter)filterList.get(0);
        OriginSubsetter filterB = (OriginSubsetter)filterList.get(1);
        StringTree[] result = new StringTree[2];
        result[0] = filterA.accept(event, eventAttr, e);
        result[1] = filterB.accept(event, eventAttr, e);
        return new StringTreeBranch(this, result[0].isSuccess() != result[1].isSuccess(),
                                    result);
    }

}// OriginXOR
