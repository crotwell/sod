package edu.sc.seis.sod.source.network;

import org.w3c.dom.Element;

import edu.iris.Fissures.model.TimeInterval;
import edu.sc.seis.sod.subsetter.AbstractSource;

public abstract class NetworkSource extends AbstractSource {

    public NetworkSource(Element config) {
        super(config);
    }

    public TimeInterval getRefreshInterval() {
        return this.refreshInterval;
    }

    protected TimeInterval refreshInterval;
}
