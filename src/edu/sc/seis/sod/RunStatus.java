/**
 * Status.java
 *
 * @author Created by Omnicore CodeGuide
 */

package edu.sc.seis.sod;

import org.apache.log4j.lf5.LogLevel;

public class RunStatus extends LogLevel{
    private RunStatus(String name){
        super(name, statusCount++);
    }

    public static final RunStatus NEW = new RunStatus("New");
    
    public static final RunStatus PASSED = new RunStatus("Passed");
    
    public static final RunStatus FAILED = new RunStatus("Failed");
    
    public static final RunStatus NETWORK_ARM = new RunStatus("Network Arm");
    
    public static final RunStatus EVENT_ARM = new RunStatus("Event Arm");
    
    public static final RunStatus WAVEFORM_ARM = new RunStatus("Waveform Arm");
    
    public static final RunStatus[] DEFAULT_STATUS = { NEW, PASSED, FAILED, NETWORK_ARM, EVENT_ARM, WAVEFORM_ARM };
    
    private static int statusCount = 0;
}

