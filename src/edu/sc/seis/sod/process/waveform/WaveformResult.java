/**
 * LocalSeismogramResult.java
 *
 * @author Philip Crotwell
 */

package edu.sc.seis.sod.process.waveform;

import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;
import edu.sc.seis.sod.status.StringTree;
import edu.sc.seis.sod.status.StringTreeLeaf;

public class WaveformResult {

    public WaveformResult(boolean success, LocalSeismogramImpl[] seismograms) {
        this(seismograms, new StringTreeLeaf("", success));
    }

    public WaveformResult(LocalSeismogramImpl[] seismograms, StringTree reason) {
        this.seismograms = seismograms;
        this.reason = reason;
    }

    public boolean isSuccess() {
        return reason.isSuccess();
    }

    public LocalSeismogramImpl[] getSeismograms() {
        return seismograms;
    }

    public StringTree getReason() {
        return reason;
    }

    private LocalSeismogramImpl[] seismograms;

    private StringTree reason;

}

