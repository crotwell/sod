package edu.sc.seis.sod.status.waveformArm;

import java.text.DecimalFormat;
import edu.sc.seis.sod.process.waveform.SaveSeismogramToFile;
import edu.sc.seis.sod.status.AllTypeTemplate;

public class SacDataWrittenTemplate extends AllTypeTemplate{
    public String getResult() {
        return df.format(SaveSeismogramToFile.getBytesWritten()/BYTES_IN_MB);
    }
    
    private static final double BYTES_IN_MB = 1024*1024;
    
    DecimalFormat df = new DecimalFormat("0.00");
}

