package edu.sc.seis.sod.process.waveform;

import org.w3c.dom.Element;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.status.StringTreeBranch;


public class RetryAndFail extends RetryAndContinue {

    public RetryAndFail(Element config) throws ConfigurationException {
        super(config);
    }

    protected WaveformResult wrapResult(WaveformResult result) {
        return new WaveformResult(result.getSeismograms(),
                                  new StringTreeBranch(this,
                                                       false,
                                                       result.getReason()));
    }

    public String toString() {
        return "RetryAndFail(" + subprocess.toString() + ")";
    }
}
