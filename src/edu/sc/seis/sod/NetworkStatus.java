/**
 * NetworkStatus.java
 *
 * @author Created by Omnicore CodeGuide
 */

package edu.sc.seis.sod;

import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.IfNetwork.NetworkAccess;
import edu.iris.Fissures.IfNetwork.Site;
import edu.iris.Fissures.IfNetwork.Station;

public interface NetworkStatus extends SodElement {
    public void setArmStatus(String status) throws Exception;

    public void change(Station station, RunStatus status) throws Exception;

    public void change(Channel channel, RunStatus status) throws Exception;

    public void change(NetworkAccess networkAccess, RunStatus status) throws Exception;

    public void change(Site site, RunStatus status) throws Exception;
}

