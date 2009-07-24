package edu.sc.seis.sod.subsetter.origin;

import java.util.Iterator;

import org.w3c.dom.Element;

import edu.iris.Fissures.IfEvent.EventAttr;
import edu.iris.Fissures.IfEvent.Origin;
import edu.sc.seis.fissuresUtil.cache.CacheEvent;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.status.ShortCircuit;
import edu.sc.seis.sod.status.StringTree;
import edu.sc.seis.sod.status.StringTreeBranch;

/**
 * Contains a single OriginSubsetter. OriginArrayAND returns true when the
 * contained originSubsetter is true for all the origins.
 */
public class OriginArrayAND extends EventLogicalSubsetter implements
        OriginSubsetter {

    public OriginArrayAND(Element config) throws ConfigurationException {
        super(config);
    }

    public StringTree accept(CacheEvent event,
                          EventAttr eventAttr,
                          Origin e) throws Exception {
        Iterator it = filterList.iterator();
        if(it.hasNext()) {
            OriginSubsetter filter = (OriginSubsetter)it.next();
            Origin[] origins = event.get_origins();
            StringTree[] result = new StringTree[origins.length];
            for(int counter = 0; counter < origins.length; counter++) {
                result[counter] = filter.accept(event, eventAttr, origins[counter]);
                if(!result[counter].isSuccess()) { 
                    for(int j = counter + 1; j < result.length; j++) {
                        result[j] = new ShortCircuit(origins[j]);
                    }
                    return new StringTreeBranch(this, false, result);
                }
                return new StringTreeBranch(this, true, result);
            }
        }
        throw new ConfigurationException("more than one subsetter inside OriginArrayAND");
    }
}// OriginArrayAND
