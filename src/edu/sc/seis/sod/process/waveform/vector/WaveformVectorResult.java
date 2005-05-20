/**
 * ChannelGroupLocalSeismogramResult.java
 *
 * @author Philip Crotwell
 */

package edu.sc.seis.sod.process.waveform.vector;

import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;
import edu.sc.seis.sod.ChannelGroup;
import edu.sc.seis.sod.status.StringTree;

public class WaveformVectorResult {

    public WaveformVectorResult(LocalSeismogramImpl[][] seismograms, StringTree reason) {
        this.seismograms = seismograms;
        this.reason = reason;
    }

    public boolean isSuccess() {
        return reason.isSuccess();
    }

    public LocalSeismogramImpl[][] getSeismograms() {
        return seismograms;
    }

    public StringTree getReason() {
        return reason;
    }

    private StringTree reason;

    private LocalSeismogramImpl[][] seismograms;
    
}

