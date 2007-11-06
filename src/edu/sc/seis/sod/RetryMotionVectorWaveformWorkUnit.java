package edu.sc.seis.sod;


public class RetryMotionVectorWaveformWorkUnit extends
MotionVectorWaveformWorkUnit {

    /** for hibernate */
    protected RetryMotionVectorWaveformWorkUnit() {}
    
    public RetryMotionVectorWaveformWorkUnit(EventVectorPair pairId) {
        super(pairId);
        logger.debug("Retrying on evp " + evp);
        Start.getWaveformArm().retryNum++;
    }

    public void run() {
        Start.getWaveformArm().retryNum--;
        super.run();
    }
    
    private static final org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(RetryMotionVectorWaveformWorkUnit.class);
}
