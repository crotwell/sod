/**
 * ChannelGroupLocalSeismogramResult.java
 *
 * @author Philip Crotwell
 */

package edu.sc.seis.sod.process.waveform.vector;

import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;
import edu.sc.seis.sod.status.StringTree;

public class WaveformVectorResult {

    public WaveformVectorResult(LocalSeismogramImpl[][] seismograms, StringTree reason) {
        this(reason.isSuccess(), seismograms, reason);
    }

    public WaveformVectorResult(boolean success, LocalSeismogramImpl[][] seismograms, StringTree reason) {
        this.success = success;
        this.seismograms = seismograms;
        this.reason = reason;
    }

    public boolean isSuccess() {
        return success;
    }

    public LocalSeismogramImpl[][] getSeismograms() {
        return seismograms;
    }

    public StringTree getReason() {
        return reason;
    }

    private StringTree reason;

    private boolean success;

    private LocalSeismogramImpl[][] seismograms;
}

