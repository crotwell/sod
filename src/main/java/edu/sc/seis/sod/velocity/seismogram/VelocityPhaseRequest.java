package edu.sc.seis.sod.velocity.seismogram;

import edu.sc.seis.sod.model.common.TimeInterval;
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
    
    public static String formatTimeInterval(TimeInterval ti) {
        String sign = "+";
        if ( ti.getValue() < 0) {
            sign = "-";
        }
        return sign+ti.getValue()+" "+ti.getUnit();
    }
    
    public String toString() {
        return getBeginPhase()+" "+getBeginOffset()+" to "+getEndPhase()+" "+getEndOffset();
    }
    
    PhaseRequest pr;
}
