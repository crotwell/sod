package edu.sc.seis.sod.status.waveformArm;

import edu.sc.seis.sod.process.waveform.SaveSeismogramToFile;
import edu.sc.seis.sod.status.AllTypeTemplate;

public class SacDataWrittenTemplate extends AllTypeTemplate{
    public String getResult() {
        return "" + SaveSeismogramToFile.getBytesWritten();
    }
}

