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
 * OriginOR.java Created: Thu Mar 14 14:02:33 2002
 * 
 * @author <a href="mailto:">Philip Crotwell </a>
 * @version
 */
public final class OriginOR extends EventLogicalSubsetter implements
        OriginSubsetter {

    public OriginOR(Element config) throws ConfigurationException {
        super(config);
    }

    public StringTree accept(CacheEvent event,
                          EventAttr eventAttr,
                          Origin e) throws Exception {
        Iterator it = filterList.iterator();
        StringTree[] result = new StringTree[filterList.size()];
        int i=0;
        while(it.hasNext()) {
            OriginSubsetter filter = (OriginSubsetter)it.next();
            result[i] = filter.accept(event, eventAttr, e);
            if(result[i].isSuccess()) { 
                for(int j = i + 1; j < result.length; j++) {
                    result[j] = new ShortCircuit(filterList.get(j));
                }
                return new StringTreeBranch(this, true, result);
            }
            i++;
        }
        return new StringTreeBranch(this, false, result);
    }
}// OriginOR
