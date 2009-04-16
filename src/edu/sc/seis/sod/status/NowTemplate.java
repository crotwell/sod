package edu.sc.seis.sod.status;


import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.TimeZone;

import edu.sc.seis.fissuresUtil.chooser.ClockUtil;

public class NowTemplate extends AllTypeTemplate{
    public NowTemplate(){ df.setTimeZone(TimeZone.getTimeZone("GMT")); }

    public String getResult() {
        return df.format(ClockUtil.now());
    }

    private DateFormat df = new SimpleDateFormat("M/d/yyyy H:mm:ss.S z");
}
