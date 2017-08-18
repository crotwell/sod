package edu.sc.seis.sod.subsetter.network;

import edu.sc.seis.seisFile.fdsnws.stationxml.Network;
import edu.sc.seis.sod.model.station.NetworkIdUtil;
import edu.sc.seis.sod.status.StringTree;
import edu.sc.seis.sod.status.StringTreeLeaf;

/**
 * @author groves Created on May 4, 2005
 */
public class TemporaryNetwork implements NetworkSubsetter {

    public StringTree accept(Network attr) {
        return new StringTreeLeaf(this, NetworkIdUtil.isTemporary(attr.get_id()));
    }

}
