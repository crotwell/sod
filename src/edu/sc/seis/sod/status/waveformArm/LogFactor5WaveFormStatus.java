/**
 * LogFactor5WaveFormStatus.java
 *
 * @author Created by Charles Groves
 */

package edu.sc.seis.sod.status.waveFormArm;

import edu.sc.seis.sod.CommonAccess;
import edu.sc.seis.sod.EventChannelPair;
import edu.sc.seis.sod.RunStatus;
import edu.sc.seis.sod.WaveFormStatus;
import org.w3c.dom.Element;

public class LogFactor5WaveFormStatus implements WaveFormStatus{
    public LogFactor5WaveFormStatus(Element config){}
    
    public void update(EventChannelPair ecp) {
        CommonAccess.getCommonAccess().getLF5Adapter().log("WaveForm Arm",
                                                           ecp.getStatus().getLogLevel(),
                                                           ecp.getInfo());
    }
}
