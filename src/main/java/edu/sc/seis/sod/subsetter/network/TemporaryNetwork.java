package edu.sc.seis.sod.subsetter.network;

import edu.iris.Fissures.IfNetwork.NetworkAttr;
import edu.iris.Fissures.network.NetworkIdUtil;
import edu.sc.seis.sod.status.StringTree;
import edu.sc.seis.sod.status.StringTreeLeaf;

/**
 * @author groves Created on May 4, 2005
 */
public class TemporaryNetwork implements NetworkSubsetter {

    public StringTree accept(NetworkAttr attr) {
        return new StringTreeLeaf(this, NetworkIdUtil.isTemporary(attr.get_id()));
    }

}
