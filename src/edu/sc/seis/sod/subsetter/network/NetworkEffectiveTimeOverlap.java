package edu.sc.seis.sod.subsetter.network;

import org.apache.log4j.Category;
import org.w3c.dom.Element;
import edu.iris.Fissures.TimeRange;
import edu.iris.Fissures.IfNetwork.NetworkAttr;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.subsetter.EffectiveTimeOverlap;

public class NetworkEffectiveTimeOverlap extends EffectiveTimeOverlap implements
        NetworkSubsetter {

    public NetworkEffectiveTimeOverlap(Element config)
            throws ConfigurationException {
        super(config);
    }

    public NetworkEffectiveTimeOverlap(TimeRange tr) {
        super(tr);
    }

    public boolean accept(NetworkAttr network) {
        return overlaps(network.effective_time);
    }

    static Category logger = Category.getInstance(NetworkEffectiveTimeOverlap.class.getName());
}// NetworkEffectiveTimeOverlap
