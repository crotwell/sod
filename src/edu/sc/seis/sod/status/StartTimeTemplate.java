/**
 * StartTimeTemplate.java
 *
 * @author Created by Omnicore CodeGuide
 */

package edu.sc.seis.sod.status;


import edu.sc.seis.sod.Start;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.TimeZone;

public class StartTimeTemplate extends AllTypeTemplate{
    public StartTimeTemplate(){ df.setTimeZone(TimeZone.getTimeZone("GMT")); }

    public String getResult() { return df.format(Start.getStartTime()); }

    private DateFormat df = new SimpleDateFormat("H:mm M/d/yyyy z");
}

