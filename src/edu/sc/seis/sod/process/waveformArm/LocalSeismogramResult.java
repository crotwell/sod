/**
 * LocalSeismogramResult.java
 *
 * @author Philip Crotwell
 */

package edu.sc.seis.sod.process.waveformArm;

import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;

public class LocalSeismogramResult {

    public LocalSeismogramResult(boolean success, LocalSeismogramImpl[] seismograms) {
        this.success = success;
        this.seismograms = seismograms;
    }

    public boolean isSuccess() {
        return success;
    }

    public LocalSeismogramImpl[] getSeismograms() {
        return seismograms;
    }

    public static final LocalSeismogramResult FAIL = new LocalSeismogramResult(false, new LocalSeismogramImpl[0]);

    private boolean success;

    private LocalSeismogramImpl[] seismograms;

}

