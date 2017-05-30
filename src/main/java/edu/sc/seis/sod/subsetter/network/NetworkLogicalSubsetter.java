package edu.sc.seis.sod.subsetter.network;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.w3c.dom.Element;

import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.model.station.NetworkAttrImpl;
import edu.sc.seis.sod.status.StringTree;
import edu.sc.seis.sod.status.StringTreeBranch;
import edu.sc.seis.sod.subsetter.LogicalSubsetter;
import edu.sc.seis.sod.subsetter.Subsetter;

public abstract class NetworkLogicalSubsetter extends LogicalSubsetter {

    public NetworkLogicalSubsetter(Element config)
            throws ConfigurationException {
        super(config);
    }

    public NetworkSubsetter[] getNetworkSubsetters() {
        return (NetworkSubsetter[])getSubsetters().toArray(new NetworkSubsetter[0]);
    }

    public static final List<String> packages = Collections.singletonList("network");
    
    public List<String> getPackages() {
        return packages;
    }

    @Override
    protected Subsetter getSubsetter(Subsetter s) throws ConfigurationException {
        return createSubsetter(s);
    }

    public static NetworkSubsetter createSubsetter(Subsetter s) throws ConfigurationException {
        if (s instanceof NetworkSubsetter) {
            return (NetworkSubsetter)s;
        }
        throw new ConfigurationException("Subsetter of type "+s.getClass()+" cannot appear here");
    }
    
    public StringTree accept(NetworkAttrImpl net) throws Exception {
        List reasons = new ArrayList(filterList.size());
        Iterator it = filterList.iterator();
        while(it.hasNext()) {
            NetworkSubsetter processor = (NetworkSubsetter)it.next();
            StringTree result;
            synchronized(processor) {
                result = processor.accept(net);
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

}// NetworkLogicalSubsetter
