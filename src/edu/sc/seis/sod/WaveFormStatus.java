/**
 * WaveFormStatus.java
 *
 * @author Created by Omnicore CodeGuide
 */

package edu.sc.seis.sod;

public interface WaveFormStatus extends SodElement {
    public void update(EventChannelPair ecp);
}

