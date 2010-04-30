package edu.sc.seis.sod.source.network;

import java.util.List;

import org.w3c.dom.Element;

import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.IfNetwork.ChannelId;
import edu.iris.Fissures.IfNetwork.ChannelNotFound;
import edu.iris.Fissures.IfNetwork.Instrumentation;
import edu.iris.Fissures.IfNetwork.NetworkId;
import edu.iris.Fissures.IfNetwork.NetworkNotFound;
import edu.iris.Fissures.IfNetwork.Sensitivity;
import edu.iris.Fissures.IfNetwork.StationId;
import edu.iris.Fissures.model.TimeInterval;
import edu.iris.Fissures.model.UnitImpl;
import edu.iris.Fissures.network.ChannelImpl;
import edu.iris.Fissures.network.NetworkAttrImpl;
import edu.iris.Fissures.network.StationImpl;
import edu.sc.seis.fissuresUtil.cache.CacheNetworkAccess;
import edu.sc.seis.fissuresUtil.cache.InstrumentationInvalid;
import edu.sc.seis.fissuresUtil.cache.ProxyNetworkDC;
import edu.sc.seis.sod.SodUtil;
import edu.sc.seis.sod.subsetter.AbstractSource;

public abstract class NetworkSource extends AbstractSource {

    public NetworkSource(String dns, String name, int retries) {
        super(dns, name, retries);
    }
    
    public NetworkSource(NetworkSource wrapped) {
        this(wrapped.getDNS(), wrapped.getName(), wrapped.getRetries());
    }
    
    public NetworkSource(Element config) throws Exception {
        super(config, "IRIS_NetworkDC");
        Element subElement = SodUtil.getElement(config, "refreshInterval");
        if(subElement != null) {
            refreshInterval = SodUtil.loadTimeInterval(subElement);
        } else {
            refreshInterval = new TimeInterval(1, UnitImpl.FORTNIGHT);
        }
    }
    
    public TimeInterval getRefreshInterval() {
        return this.refreshInterval;
    }

    protected TimeInterval refreshInterval;
    
    protected String[] constrainingCodes;
    
    public void setConstrainingNetworkCodes(String[] constrainingCodes) {
        this.constrainingCodes = constrainingCodes;
    }
    
    public String[] getConstrainingNetworkCodes() {
        return constrainingCodes;
    }


    public abstract CacheNetworkAccess getNetwork(NetworkAttrImpl attr);
    
    public abstract List<CacheNetworkAccess> getNetworkByName(String name) throws NetworkNotFound;
    
    public abstract List<CacheNetworkAccess> getNetworks();
    
    public abstract List<StationImpl> getStations(NetworkId net);
    
    public abstract List<ChannelImpl> getChannels(StationImpl station);

    public abstract Sensitivity getSensitivity(ChannelId chanId) throws ChannelNotFound, InstrumentationInvalid;
    
    public abstract Instrumentation getInstrumentation(ChannelId chanId) throws ChannelNotFound, InstrumentationInvalid;
    
}
