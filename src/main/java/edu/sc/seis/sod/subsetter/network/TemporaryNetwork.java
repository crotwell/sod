package edu.sc.seis.sod.subsetter.network;

import edu.sc.seis.sod.model.station.NetworkAttrImpl;
import edu.sc.seis.sod.model.station.NetworkIdUtil;
import edu.sc.seis.sod.status.StringTree;
import edu.sc.seis.sod.status.StringTreeLeaf;

/**
 * @author groves Created on May 4, 2005
 */
public class TemporaryNetwork implements NetworkSubsetter {

    public StringTree accept(NetworkAttrImpl attr) {
        return new StringTreeLeaf(this, NetworkIdUtil.isTemporary(attr.get_id()));
    }

}
