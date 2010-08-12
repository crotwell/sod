package edu.sc.seis.sod.source.network;

import java.util.List;

import org.w3c.dom.Element;

import edu.iris.Fissures.IfNetwork.ChannelId;
import edu.iris.Fissures.IfNetwork.ChannelNotFound;
import edu.iris.Fissures.IfNetwork.Instrumentation;
import edu.iris.Fissures.IfNetwork.NetworkId;
import edu.iris.Fissures.IfNetwork.NetworkNotFound;
import edu.iris.Fissures.IfNetwork.Sensitivity;
import edu.iris.Fissures.model.TimeInterval;
import edu.iris.Fissures.model.UnitImpl;
import edu.iris.Fissures.network.ChannelImpl;
import edu.iris.Fissures.network.NetworkAttrImpl;
import edu.iris.Fissures.network.StationImpl;
import edu.sc.seis.fissuresUtil.cache.CacheNetworkAccess;
import edu.sc.seis.fissuresUtil.cache.InstrumentationInvalid;
import edu.sc.seis.sod.SodUtil;
import edu.sc.seis.sod.source.AbstractSource;

public abstract class AbstractNetworkSource extends AbstractSource implements NetworkSource {

    public AbstractNetworkSource(String dns, String name, int retries) {
        super(dns, name, retries);
        refreshInterval = new TimeInterval(1, UnitImpl.FORTNIGHT);
    }
    
    public AbstractNetworkSource(AbstractNetworkSource wrapped) {
        this(wrapped.getDNS(), wrapped.getName(), wrapped.getRetries());
    }
    
    public AbstractNetworkSource(Element config) throws Exception {
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

    /* (non-Javadoc)
     * @see edu.sc.seis.sod.source.network.NetworkSource#getNetwork(edu.iris.Fissures.network.NetworkAttrImpl)
     */
    public abstract CacheNetworkAccess getNetwork(NetworkAttrImpl attr);
    
    /* (non-Javadoc)
     * @see edu.sc.seis.sod.source.network.NetworkSource#getNetworkByName(java.lang.String)
     */
    public abstract List<? extends CacheNetworkAccess> getNetworkByName(String name) throws NetworkNotFound;
    
    /* (non-Javadoc)
     * @see edu.sc.seis.sod.source.network.NetworkSource#getNetworks()
     */
    public abstract List<? extends CacheNetworkAccess> getNetworks();
    
    /* (non-Javadoc)
     * @see edu.sc.seis.sod.source.network.NetworkSource#getStations(edu.iris.Fissures.IfNetwork.NetworkId)
     */
    public abstract List<? extends StationImpl> getStations(NetworkId net);
    
    /* (non-Javadoc)
     * @see edu.sc.seis.sod.source.network.NetworkSource#getChannels(edu.iris.Fissures.network.StationImpl)
     */
    public abstract List<? extends ChannelImpl> getChannels(StationImpl station);

    /* (non-Javadoc)
     * @see edu.sc.seis.sod.source.network.NetworkSource#getSensitivity(edu.iris.Fissures.IfNetwork.ChannelId)
     */
    public abstract Sensitivity getSensitivity(ChannelId chanId) throws ChannelNotFound, InstrumentationInvalid;
    
    /* (non-Javadoc)
     * @see edu.sc.seis.sod.source.network.NetworkSource#getInstrumentation(edu.iris.Fissures.IfNetwork.ChannelId)
     */
    public abstract Instrumentation getInstrumentation(ChannelId chanId) throws ChannelNotFound, InstrumentationInvalid;
    
}
