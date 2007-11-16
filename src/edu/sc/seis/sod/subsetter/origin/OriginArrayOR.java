package edu.sc.seis.sod.subsetter.origin;

import edu.iris.Fissures.IfEvent.EventAttr;
import edu.iris.Fissures.IfEvent.Origin;
import edu.sc.seis.fissuresUtil.cache.CacheEvent;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.status.ShortCircuit;
import edu.sc.seis.sod.status.StringTree;
import edu.sc.seis.sod.status.StringTreeBranch;
import java.util.Iterator;
import org.w3c.dom.Element;
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

    public StringTree accept(CacheEvent event, EventAttr eventAttr, Origin e) throws Exception{
        Iterator it = filterList.iterator();
        if (it.hasNext()) {
            OriginSubsetter filter = (OriginSubsetter)it.next();
            Origin[] origins = event.get_origins();
            StringTree[] result = new StringTree[origins.length];
            for(int counter = 0; counter < origins.length; counter++) {
                result[counter] = filter.accept(event, eventAttr, origins[counter]);
                if (result[counter].isSuccess()) { 
                    for(int j = counter + 1; j < result.length; j++) {
                        result[j] = new ShortCircuit(origins[j]);
                    }
                    return new StringTreeBranch(this, true, result);
                }
            }
            return new StringTreeBranch(this, false, result);
        }
        throw new ConfigurationException("more than one subsetter in OriginArrayOR");
    }
}// OriginArrayOR
