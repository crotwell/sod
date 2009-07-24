package edu.sc.seis.sod.process.waveform.vector;


/**
 * @author groves
 * Created on Dec 1, 2004
 */
public interface WaveformVectorProcessWrapper extends WaveformVectorProcess {
    public WaveformVectorProcess[] getWrappedProcessors();
}
