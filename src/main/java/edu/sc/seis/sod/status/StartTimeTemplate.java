/**
 * StartTimeTemplate.java
 *
 * @author Created by Omnicore CodeGuide
 */

package edu.sc.seis.sod.status;


import java.util.TimeZone;

import edu.sc.seis.fissuresUtil.chooser.ThreadSafeSimpleDateFormat;
import edu.sc.seis.sod.Start;

public class StartTimeTemplate extends AllTypeTemplate{

    public String getResult() { return df.format(Start.getStartTime()); }

    private ThreadSafeSimpleDateFormat df = new ThreadSafeSimpleDateFormat("M/d/yyyy H:mm z", TimeZone.getTimeZone("GMT"));
}

