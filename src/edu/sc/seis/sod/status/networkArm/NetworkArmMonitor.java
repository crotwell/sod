/**
 * NetworkStatus.java
 *
 * @author Created by Omnicore CodeGuide
 */

package edu.sc.seis.sod.status.networkArm;
import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.IfNetwork.NetworkAccess;
import edu.iris.Fissures.IfNetwork.Site;
import edu.iris.Fissures.IfNetwork.Station;
import edu.sc.seis.sod.SodElement;
import edu.sc.seis.sod.Status;

public interface NetworkArmMonitor extends SodElement {
    public void setArmStatus(String status) throws Exception;

    public void change(Station station, Status s);

    public void change(Channel channel, Status s);

    public void change(NetworkAccess networkAccess, Status s);

    public void change(Site site, Status s);
}

