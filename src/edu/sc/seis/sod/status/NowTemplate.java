package edu.sc.seis.sod.status;


import edu.sc.seis.fissuresUtil.chooser.ClockUtil;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

public class NowTemplate extends AllTypeTemplate{
    public String getResult() {
        return df.format(ClockUtil.now());
    }
    
    private DateFormat df = new SimpleDateFormat("h:mm:ss.S a M/d/yyyy");
}
