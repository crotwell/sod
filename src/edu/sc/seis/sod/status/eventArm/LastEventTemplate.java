package edu.sc.seis.sod.status.eventArm;

import edu.sc.seis.sod.Start;
import edu.sc.seis.sod.status.AllTypeTemplate;

public class LastEventTemplate extends AllTypeTemplate{
    public String getResult() {
        if(Start.getEventArm() != null){
            return Start.getEventArm().getLastEvent();
        }else{ return "None"; }
    }
}
