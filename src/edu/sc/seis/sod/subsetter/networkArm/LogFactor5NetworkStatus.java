/**
 * LogFactor5Status.java
 *
 * @author Created by Omnicore CodeGuide
 */

package edu.sc.seis.sod.subsetter.networkArm;

import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.IfNetwork.NetworkAccess;
import edu.iris.Fissures.IfNetwork.Site;
import edu.iris.Fissures.IfNetwork.Station;
import edu.iris.Fissures.network.ChannelIdUtil;
import edu.iris.Fissures.network.NetworkIdUtil;
import edu.iris.Fissures.network.SiteIdUtil;
import edu.iris.Fissures.network.StationIdUtil;
import edu.sc.seis.sod.CommonAccess;
import edu.sc.seis.sod.NetworkStatus;
import edu.sc.seis.sod.RunStatus;
import org.w3c.dom.Element;

public class LogFactor5NetworkStatus implements NetworkStatus{
    
    public LogFactor5NetworkStatus(Element config){}
    
    public void change(NetworkAccess networkAccess, RunStatus newStatus) {
        CommonAccess.getCommonAccess().getLF5Adapter().log("Network Arm.NetworkAccess", newStatus.getLogLevel(),
                                                           newStatus.toString() + " " + NetworkIdUtil.toString(networkAccess.get_attributes().get_id()));
    }
    
    public void change(Station station, RunStatus newStatus) {
        CommonAccess.getCommonAccess().getLF5Adapter().log("Network Arm.Station", newStatus.getLogLevel(),
                                                           newStatus.toString() + " " + StationIdUtil.toString(station.get_id()));
    }
    
    public void change(Site site, RunStatus newStatus) {
        CommonAccess.getCommonAccess().getLF5Adapter().log("Network Arm.Site", newStatus.getLogLevel(),
                                                           newStatus.toString() + " " + SiteIdUtil.toString(site.get_id()));
    }
    
    public void change(Channel channel, RunStatus newStatus) {
        CommonAccess.getCommonAccess().getLF5Adapter().log("Network Arm.Channel", newStatus.getLogLevel(),
                                                           newStatus.toString() + " " + ChannelIdUtil.toString(channel.get_id()));
    }
    
    public void setArmStatus(String status) {
        CommonAccess.getCommonAccess().getLF5Adapter().log("Network Arm", RunStatus.GENERIC.getLogLevel(),
                                                           status);
    }
}

