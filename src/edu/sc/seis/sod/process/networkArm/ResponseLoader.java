/**
 * ResponseLoader.java
 *
 * @author Created by Omnicore CodeGuide
 */

package edu.sc.seis.sod.process.networkArm;

import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.IfNetwork.Instrumentation;
import edu.iris.Fissures.IfNetwork.NetworkAccess;
import edu.iris.Fissures.IfNetwork.Site;
import edu.iris.Fissures.IfNetwork.Station;
import edu.sc.seis.fissuresUtil.bag.ResponseGain;
import edu.sc.seis.sod.Status;
import edu.sc.seis.sod.status.networkArm.NetworkArmMonitor;
import edu.iris.Fissures.IfNetwork.ChannelNotFound;
import edu.iris.Fissures.IfNetwork.NetworkNotFound;

public class ResponseLoader implements NetworkArmMonitor {

    public ResponseLoader(ResponseGain responseGain) {
        this.responseGain = responseGain;
    }

    public void setArmStatus(String status) throws Exception {}

    public void change(Station station, Status s) {}

    public void change(Channel channel, Status s) {
        try {
            Instrumentation inst = responseGain.getInstrumentation(channel.get_id(), channel.effective_time.start_time);
        } catch (ChannelNotFound e) {} catch (NetworkNotFound e) {}
    }

    public void change(NetworkAccess networkAccess, Status s) {}

    public void change(Site site, Status s) {}

    protected ResponseGain responseGain;

}

