package edu.sc.seis.sod.subsetter.network;

import edu.iris.Fissures.IfNetwork.NetworkAttr;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.subsetter.EffectiveTimeOverlap;
import org.apache.log4j.Category;
import org.w3c.dom.Element;

public class NetworkEffectiveTimeOverlap extends EffectiveTimeOverlap
    implements NetworkSubsetter {

    public NetworkEffectiveTimeOverlap (Element config) throws ConfigurationException{ super(config); }

    public boolean accept(NetworkAttr network) {
        return overlaps(network.effective_time);
    }

    static Category logger =
        Category.getInstance(NetworkEffectiveTimeOverlap.class.getName());
}// NetworkEffectiveTimeOverlap
