/**
 * ChannelGroupLocalSeismogramResult.java
 *
 * @author Philip Crotwell
 */

package edu.sc.seis.sod.process.waveform.vector;

import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;
import edu.sc.seis.sod.status.StringTree;
import edu.sc.seis.sod.status.StringTreeLeaf;

public class WaveformVectorResult {

    public WaveformVectorResult(boolean success, LocalSeismogramImpl[][] seismograms, Object actor) {
        this(seismograms, new StringTreeLeaf(actor, success));
    }
    
    public WaveformVectorResult(boolean success, LocalSeismogramImpl[][] seismograms, Object actor, String reason) {
        this(seismograms, new StringTreeLeaf(actor, success, reason));
    }
    
    /** convenience constructor to get around two dimensional arrays being hard in jython. */ 
    public WaveformVectorResult(boolean success, LocalSeismogramImpl[] seismogramsA, LocalSeismogramImpl[] seismogramsB, LocalSeismogramImpl[] seismogramsC, Object actor) {
        this(success, new LocalSeismogramImpl[][] {seismogramsA, seismogramsB, seismogramsC}, actor);
    }

    /** convenience constructor to get around two dimensional arrays being hard in jython. */ 
    public WaveformVectorResult(boolean success, LocalSeismogramImpl[] seismogramsA, LocalSeismogramImpl[] seismogramsB, LocalSeismogramImpl[] seismogramsC, Object actor, String reason) {
        this(success, new LocalSeismogramImpl[][] {seismogramsA, seismogramsB, seismogramsC}, actor, reason);
    }
    
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

