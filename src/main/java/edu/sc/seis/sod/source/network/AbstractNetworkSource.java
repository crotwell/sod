package edu.sc.seis.sod.source.network;

import org.w3c.dom.Element;

import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.SodUtil;
import edu.sc.seis.sod.model.common.TimeInterval;
import edu.sc.seis.sod.model.common.UnitImpl;
import edu.sc.seis.sod.source.AbstractSource;

public abstract class AbstractNetworkSource extends AbstractSource implements NetworkSource {

    public AbstractNetworkSource(String name, int retries) {
        super(name, retries);
        refreshInterval = new TimeInterval(1, UnitImpl.FORTNIGHT);
    }
    
    public AbstractNetworkSource(NetworkSource wrapped) {
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
