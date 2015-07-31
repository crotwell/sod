package edu.sc.seis.sod.source.network;

import java.net.URL;

import edu.iris.Fissures.model.MicroSecondDate;
import edu.sc.seis.fissuresUtil.chooser.ClockUtil;


public class TimeQuery {
    
    public TimeQuery(URL url) {
        this.url = url;
        this.time = ClockUtil.now();
    }
    
    URL url;
    MicroSecondDate time;
}
