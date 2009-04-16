package edu.sc.seis.sod.subsetter.origin;

import java.util.Iterator;

import org.w3c.dom.Element;

import edu.iris.Fissures.IfEvent.EventAttr;
import edu.iris.Fissures.IfEvent.Origin;
import edu.sc.seis.fissuresUtil.cache.CacheEvent;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.status.StringTree;
import edu.sc.seis.sod.status.StringTreeBranch;

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

    public StringTree accept(CacheEvent event, EventAttr eventAttr, Origin e) throws Exception{
        Iterator it = filterList.iterator();
        if (it.hasNext()) {
            OriginSubsetter filter = (OriginSubsetter)it.next();
            StringTree result = filter.accept(event, eventAttr, e);
            if (result.isSuccess()) { return new StringTreeBranch(this, false, result); }
            return new StringTreeBranch(this, true, result);
        }
        throw new ConfigurationException("empty NOT");
    }
}// OriginNOT
