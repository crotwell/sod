package edu.sc.seis.sod.status.eventArm;

import edu.iris.Fissures.IfEvent.EventAccessOperations;

public interface EventTemplate{
    public String getResult(EventAccessOperations ev);
}

