/**
 * WaveFormStatus.java
 *
 * @author Created by Omnicore CodeGuide
 */

package edu.sc.seis.sod.status.waveFormArm;
import edu.sc.seis.sod.*;

public interface WaveformArmMonitor extends SodElement {
    public void update(EventChannelPair ecp) throws Exception;
}

