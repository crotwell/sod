package edu.sc.seis.sod.subsetter;

import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;
import edu.sc.seis.sod.process.waveform.WaveformResult;
import edu.sc.seis.sod.process.waveform.vector.WaveformVectorResult;
import edu.sc.seis.sod.status.Fail;
import edu.sc.seis.sod.status.Pass;


public class ScriptUtil {
    
    public ScriptUtil(Subsetter subsetter) {
        this.subsetter = subsetter;
    }
    
    public Pass pass() {
        return new Pass(subsetter);
    }
    
    public Pass pass(String reason) {
        return new Pass(subsetter, reason);
    }
    
    public Fail fail() {
        return new Fail(subsetter);
    }
    
    public Fail fail(String reason) {
        return new Fail(subsetter, reason);
    }
    
    public Fail fail(String reason, Throwable exception) {
        return new Fail(subsetter, reason, exception);
    }

    public WaveformResult waveformResult(boolean result, LocalSeismogramImpl[] seis) {
        return new WaveformResult(result, seis, subsetter);
    }

    public WaveformVectorResult waveformVectorResult(boolean result, LocalSeismogramImpl[][] seis) {
        return new WaveformVectorResult(result, seis, subsetter);
    }
    
    Subsetter subsetter;
}
