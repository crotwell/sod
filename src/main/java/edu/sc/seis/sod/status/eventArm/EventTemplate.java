package edu.sc.seis.sod.status.eventArm;

import edu.sc.seis.sod.model.event.CacheEvent;

public interface EventTemplate{
    public String getResult(CacheEvent ev);
}

