/**
 * StartTimeTemplate.java
 *
 * @author Created by Omnicore CodeGuide
 */

package edu.sc.seis.sod.status;


import edu.sc.seis.sod.Start;

public class StartTimeTemplate extends AllTypeTemplate{
    public String getResult() { return Start.getStartTime().toString(); }
}

