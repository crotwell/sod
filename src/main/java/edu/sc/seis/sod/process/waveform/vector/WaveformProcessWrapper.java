package edu.sc.seis.sod.process.waveform.vector;

import edu.sc.seis.sod.Threadable;
import edu.sc.seis.sod.process.waveform.WaveformProcess;


/**
 * @author groves
 * Created on Dec 1, 2004
 */
public interface WaveformProcessWrapper extends WaveformVectorProcess, Threadable {
    public WaveformProcess getWrappedProcess();
}
