package edu.sc.seis.sod.subsetter.site;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.w3c.dom.Element;
import edu.iris.Fissures.IfNetwork.NetworkAccess;
import edu.iris.Fissures.IfNetwork.Site;
import edu.iris.Fissures.IfNetwork.Station;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.status.StringTree;
import edu.sc.seis.sod.status.StringTreeBranch;
import edu.sc.seis.sod.subsetter.LogicalLoaderSubsetter;
import edu.sc.seis.sod.subsetter.LogicalSubsetter;
import edu.sc.seis.sod.subsetter.SubsetterLoader;
import edu.sc.seis.sod.subsetter.station.StationSubsetter;

/**
 * @author groves Created on Aug 30, 2004
 */
public abstract class SiteLogicalSubsetter extends LogicalSubsetter {

    public SiteLogicalSubsetter(Element config) throws ConfigurationException {
        super(config);
    }

    public SiteSubsetter[] getSubsetters() {
        return (SiteSubsetter[])filterList.toArray(new SiteSubsetter[0]);
    }

    public String getPackage() {
        return "site";
    }

    public StringTree accept(Site sta, NetworkAccess network) throws Exception {
        List reasons = new ArrayList(filterList.size());
        Iterator it = filterList.iterator();
        while(it.hasNext()) {
            SiteSubsetter processor = (SiteSubsetter)it.next();
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