package edu.sc.seis.sod.status;


import java.util.TimeZone;

import edu.sc.seis.fissuresUtil.chooser.ClockUtil;
import edu.sc.seis.fissuresUtil.chooser.ThreadSafeSimpleDateFormat;

public class NowTemplate extends AllTypeTemplate{

    public String getResult() {
        return df.format(ClockUtil.now());
    }

    private ThreadSafeSimpleDateFormat df = new ThreadSafeSimpleDateFormat("M/d/yyyy H:mm:ss.S z", TimeZone.getTimeZone("GMT"));
}
