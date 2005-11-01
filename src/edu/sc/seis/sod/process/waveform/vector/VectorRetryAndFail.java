package edu.sc.seis.sod.process.waveform.vector;

import org.w3c.dom.Element;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.status.StringTreeBranch;

public class VectorRetryAndFail extends VectorRetryAndContinue {

    public VectorRetryAndFail(Element config) throws ConfigurationException {
        super(config);
    }

    public WaveformVectorResult wrap(WaveformVectorResult result) {
        result = new WaveformVectorResult(result.getSeismograms(),
                                          new StringTreeBranch(this,
                                                               false,
                                                               result.getReason()));
        return result;
    }
}
