package edu.sc.seis.sod.subsetter.station;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.w3c.dom.Element;

import edu.iris.Fissures.network.StationImpl;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.source.network.NetworkSource;
import edu.sc.seis.sod.status.StringTree;
import edu.sc.seis.sod.status.StringTreeBranch;
import edu.sc.seis.sod.subsetter.LogicalSubsetter;
import edu.sc.seis.sod.subsetter.Subsetter;
import edu.sc.seis.sod.subsetter.network.NetworkLogicalSubsetter;
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

    public static final List<String> packages;
    
    static {
        packages = new LinkedList<String>();
        packages.add("station");
        packages.addAll(NetworkLogicalSubsetter.packages);
    }
    
    public List<String> getPackages() {
        return packages;
    }

    protected Subsetter getSubsetter(Subsetter s) throws ConfigurationException {
        return createSubsetter(s);
    }
    
    public StringTree accept(StationImpl sta, NetworkSource network) throws Exception {
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

    public static StationSubsetter createSubsetter(final Subsetter s) throws ConfigurationException {
        if (s instanceof StationSubsetter) {
            return (StationSubsetter)s;
        }
        return new StationSubsetter() {
            NetworkSubsetter ns = NetworkLogicalSubsetter.createSubsetter(s);

            public StringTree accept(StationImpl station, NetworkSource network) throws Exception {
                return ns.accept(station.getNetworkAttr());
            }
            
        };
    }
}