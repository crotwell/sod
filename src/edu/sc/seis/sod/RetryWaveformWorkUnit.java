package edu.sc.seis.sod;


public class RetryWaveformWorkUnit extends LocalSeismogramWaveformWorkUnit {

    /** for hibernate */
    protected RetryWaveformWorkUnit() {}
    
    public RetryWaveformWorkUnit(EventChannelPair ecp) {
        super(ecp);
        logger.debug("Retrying on ecp " + ecp);
        Start.getWaveformArm().retryNum++;
    }

    public void run() {
        Start.getWaveformArm().retryNum--;
        super.run();
    }

    public boolean equals(Object o) {
        return super.equals(o) && o instanceof RetryWaveformWorkUnit;
    }
    
    private static final org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(RetryWaveformWorkUnit.class);
}