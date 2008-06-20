package edu.sc.seis.sod.subsetter.network;

import org.apache.log4j.Category;
import org.w3c.dom.Element;
import edu.iris.Fissures.TimeRange;
import edu.iris.Fissures.IfNetwork.NetworkAttr;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.status.StringTree;
import edu.sc.seis.sod.status.StringTreeLeaf;
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

    public StringTree accept(NetworkAttr network) {
        return new StringTreeLeaf(this, overlaps(network.getEffectiveTime()));
    }

    static Category logger = Category.getInstance(NetworkEffectiveTimeOverlap.class.getName());
}// NetworkEffectiveTimeOverlap
