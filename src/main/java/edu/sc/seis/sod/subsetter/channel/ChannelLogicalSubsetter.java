package edu.sc.seis.sod.subsetter.channel;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.w3c.dom.Element;

import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.model.station.ChannelImpl;
import edu.sc.seis.sod.model.station.StationImpl;
import edu.sc.seis.sod.source.network.NetworkSource;
import edu.sc.seis.sod.status.StringTree;
import edu.sc.seis.sod.status.StringTreeBranch;
import edu.sc.seis.sod.subsetter.Subsetter;
import edu.sc.seis.sod.subsetter.station.StationLogicalSubsetter;
import edu.sc.seis.sod.subsetter.station.StationSubsetter;

/**
 * @author groves Created on Aug 30, 2004
 */
public abstract class ChannelLogicalSubsetter extends CompositeChannelSubsetter {

    public ChannelLogicalSubsetter(Element config)
            throws ConfigurationException {
        super(config);
    }

    public static final List<String> packages;
    
    static {
        packages = new LinkedList<String>();
        packages.add("channel");
        packages.addAll(StationLogicalSubsetter.packages);
    }
    
    public StringTree accept(ChannelImpl e, NetworkSource network)
            throws Exception {
        List reasons = new ArrayList(subsetters.size());
        Iterator it = subsetters.iterator();
        while(it.hasNext()) {
            ChannelSubsetter processor = (ChannelSubsetter)it.next();
            StringTree result;
            synchronized(processor) {
                result = processor.accept(e, network);
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


    public static ChannelSubsetter createSubsetter(final Subsetter s) throws ConfigurationException {
        if (s instanceof ChannelSubsetter) {
            return (ChannelSubsetter)s;
        }
        return new ChannelSubsetter() {
            StationSubsetter ss = StationLogicalSubsetter.createSubsetter(s);
            public StringTree accept(ChannelImpl channel, NetworkSource network) throws Exception {
                return ss.accept((StationImpl)channel.getStation(), network);
            }
        };
    }
}