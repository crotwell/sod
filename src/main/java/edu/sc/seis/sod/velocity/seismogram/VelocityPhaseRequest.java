package edu.sc.seis.sod.velocity.seismogram;

import java.time.Duration;

import edu.sc.seis.seisFile.TimeUtils;
import edu.sc.seis.sod.subsetter.requestGenerator.PhaseRequest;


public class VelocityPhaseRequest {
    
    public VelocityPhaseRequest(PhaseRequest pr) {
        this.pr = pr;
    }
    
    public String getBeginPhase() {
        return pr.getPhaseReq().getBeginPhase();
    }
    
    public String getEndPhase() {
        return pr.getPhaseReq().getEndPhase();
    }
    
    public String getBeginOffset() {
        return formatTimeInterval( pr.getPhaseReq().getBeginOffset());
    }

    public String getEndOffset() {
        return formatTimeInterval( pr.getPhaseReq().getEndOffset());
    }
    
    public static String formatTimeInterval(Duration ti) {
        String sign = "+";
        if ( ti.toNanos() < 0) {
            sign = "-";
        }
        return sign+TimeUtils.durationToDoubleSeconds(ti)+" sec";
    }
    
    public String toString() {
        return getBeginPhase()+" "+getBeginOffset()+" to "+getEndPhase()+" "+getEndOffset();
    }
    
    PhaseRequest pr;
}
