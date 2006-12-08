package edu.sc.seis.sod.subsetter.station;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.w3c.dom.Element;
import edu.iris.Fissures.IfNetwork.NetworkAccess;
import edu.iris.Fissures.IfNetwork.NetworkAttr;
import edu.iris.Fissures.IfNetwork.Station;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.status.StringTree;
import edu.sc.seis.sod.status.StringTreeBranch;
import edu.sc.seis.sod.subsetter.LogicalLoaderSubsetter;
import edu.sc.seis.sod.subsetter.LogicalSubsetter;
import edu.sc.seis.sod.subsetter.SubsetterLoader;
import edu.sc.seis.sod.subsetter.network.NetworkSubsetter;

/**
 * @author groves Created on Aug 30, 2004
 */
public abstract class StationLogicalSubsetter extends LogicalSubsetter {

    public StationLogicalSubsetter(Element config)
            throws ConfigurationException {
        super(config);
    }

    public StationSubsetter[] getSubsetters() {
        return (StationSubsetter[])filterList.toArray(new StationSubsetter[0]);
    }

    public String getArmName() {
        return "network";
    }

    public StringTree accept(Station sta, NetworkAccess network) throws Exception {
        List reasons = new ArrayList(filterList.size());
        Iterator it = filterList.iterator();
        while(it.hasNext()) {
            StationSubsetter processor = (StationSubsetter)it.next();
            StringTree result;
            synchronized(processor) {
                result = processor.accept(sta, network);
            }
            reasons.add(result);
            if(!shouldContinue(result)) {
                break;
            }
        } // end of while (it.hasNext())
        StringTree[] results = (StringTree[])reasons.toArray(new StringTree[0]);
        return new StringTreeBranch(this, isSuccess(results), results);
    }

    public abstract boolean shouldContinue(StringTree result);

    public abstract boolean isSuccess(StringTree[] reasons);
}