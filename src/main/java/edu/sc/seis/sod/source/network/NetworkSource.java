package edu.sc.seis.sod.source.network;

import org.w3c.dom.Element;

import edu.iris.Fissures.model.TimeInterval;
import edu.iris.Fissures.model.UnitImpl;
import edu.sc.seis.sod.SodUtil;
import edu.sc.seis.sod.subsetter.AbstractSource;

public abstract class NetworkSource extends AbstractSource {

    public NetworkSource(Element config) throws Exception {
        super(config);
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
}
