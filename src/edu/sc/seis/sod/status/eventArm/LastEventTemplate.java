/**
 * LastEventTemplate.java
 *
 * @author Created by Omnicore CodeGuide
 */

package edu.sc.seis.sod.status.eventArm;

import edu.sc.seis.sod.Start;
import edu.sc.seis.sod.status.AllTypeTemplate;

public class LastEventTemplate extends AllTypeTemplate{
    public String getResult() {
        return Start.getEventArm().getLastEvent();
    }
}

