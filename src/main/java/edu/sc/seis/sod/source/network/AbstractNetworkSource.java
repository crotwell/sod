package edu.sc.seis.sod.source.network;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Element;

import edu.iris.Fissures.IfNetwork.ChannelId;
import edu.iris.Fissures.IfNetwork.ChannelNotFound;
import edu.iris.Fissures.IfNetwork.Instrumentation;
import edu.iris.Fissures.IfNetwork.NetworkId;
import edu.iris.Fissures.IfNetwork.NetworkNotFound;
import edu.iris.Fissures.model.QuantityImpl;
import edu.iris.Fissures.model.TimeInterval;
import edu.iris.Fissures.model.UnitImpl;
import edu.iris.Fissures.network.ChannelImpl;
import edu.iris.Fissures.network.NetworkAttrImpl;
import edu.iris.Fissures.network.StationImpl;
import edu.sc.seis.fissuresUtil.cache.CacheNetworkAccess;
import edu.sc.seis.fissuresUtil.sac.InvalidResponse;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.SodUtil;
import edu.sc.seis.sod.source.AbstractSource;

public abstract class AbstractNetworkSource extends AbstractSource implements NetworkSource {

    public AbstractNetworkSource(String name, int retries) {
        super(name, retries);
        refreshInterval = new TimeInterval(1, UnitImpl.FORTNIGHT);
    }
    
    public AbstractNetworkSource(AbstractNetworkSource wrapped) {
        this(wrapped.getName(), wrapped.getRetries());
    }
    
    public AbstractNetworkSource(Element config) throws ConfigurationException  {
        super(config, "default", -1);
        Element subElement = SodUtil.getElement(config, REFRESH_ELEMENT);
        if(subElement != null) {
            refreshInterval = SodUtil.loadTimeInterval(subElement);
        } else {
            refreshInterval = new TimeInterval(1, UnitImpl.FORTNIGHT);
        }
    }
    
    public TimeInterval getRefreshInterval() {
        return this.refreshInterval;
    }
    
    public void setConstraints(NetworkQueryConstraints constraints) {
        this.constraints = constraints;
    }
    
    protected NetworkQueryConstraints constraints;
    
    protected TimeInterval refreshInterval;    
    
    public static final String REFRESH_ELEMENT = "refreshInterval";
    
}
