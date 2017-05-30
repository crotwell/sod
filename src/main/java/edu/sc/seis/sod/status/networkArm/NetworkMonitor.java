/**
 * NetworkStatus.java
 *
 * @author Created by Omnicore CodeGuide
 */

package edu.sc.seis.sod.status.networkArm;
import edu.sc.seis.sod.SodElement;
import edu.sc.seis.sod.model.station.ChannelImpl;
import edu.sc.seis.sod.model.station.NetworkAttrImpl;
import edu.sc.seis.sod.model.station.SiteImpl;
import edu.sc.seis.sod.model.station.StationImpl;
import edu.sc.seis.sod.model.status.Status;

public interface NetworkMonitor extends SodElement {
    public void setArmStatus(String status) throws Exception;

    public void change(StationImpl station, Status s);

    public void change(ChannelImpl channel, Status s);

    public void change(NetworkAttrImpl networkAccess, Status s);

    public void change(SiteImpl site, Status s);
}

