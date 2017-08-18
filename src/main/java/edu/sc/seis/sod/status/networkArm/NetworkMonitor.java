/**
 * NetworkStatus.java
 *
 * @author Created by Omnicore CodeGuide
 */

package edu.sc.seis.sod.status.networkArm;
import edu.sc.seis.seisFile.fdsnws.stationxml.Channel;
import edu.sc.seis.seisFile.fdsnws.stationxml.Network;
import edu.sc.seis.seisFile.fdsnws.stationxml.Station;
import edu.sc.seis.sod.SodElement;
import edu.sc.seis.sod.model.station.SiteImpl;
import edu.sc.seis.sod.model.status.Status;

public interface NetworkMonitor extends SodElement {
    public void setArmStatus(String status) throws Exception;

    public void change(Station station, Status s);

    public void change(Channel channel, Status s);

    public void change(Network networkAccess, Status s);

    public void change(SiteImpl site, Status s);
}

