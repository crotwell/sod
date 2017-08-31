package edu.sc.seis.sod.source.network;

import java.time.Duration;

import org.w3c.dom.Element;

import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.SodUtil;
import edu.sc.seis.sod.model.common.TimeInterval;
import edu.sc.seis.sod.model.common.UnitImpl;
import edu.sc.seis.sod.source.AbstractSource;

public abstract class AbstractNetworkSource extends AbstractSource implements NetworkSource {

    public AbstractNetworkSource(String name, int retries) {
        super(name, retries);
        refreshInterval = FORTNIGHT;
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
            refreshInterval = FORTNIGHT;
        }
    }
    
    public Duration getRefreshInterval() {
        return this.refreshInterval;
    }
    
    public void setConstraints(NetworkQueryConstraints constraints) {
        this.constraints = constraints;
    }
    
    protected NetworkQueryConstraints constraints;
    
    protected Duration refreshInterval;    
    
    Duration FORTNIGHT = Duration.ofDays(14);
   
    public static final String REFRESH_ELEMENT = "refreshInterval";
    
}
