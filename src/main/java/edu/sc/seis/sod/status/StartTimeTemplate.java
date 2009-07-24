/**
 * StartTimeTemplate.java
 *
 * @author Created by Omnicore CodeGuide
 */

package edu.sc.seis.sod.status;


import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.TimeZone;

import edu.sc.seis.sod.Start;

public class StartTimeTemplate extends AllTypeTemplate{
    public StartTimeTemplate(){ df.setTimeZone(TimeZone.getTimeZone("GMT")); }

    public String getResult() { return df.format(Start.getStartTime()); }

    private DateFormat df = new SimpleDateFormat("M/d/yyyy H:mm z");
}

