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
    
    public static final RunStatus GENERIC_STATUS = new RunStatus("Info");
    
    public static final RunStatus[] DEFAULT_STATUS = { NEW, PASSED, FAILED, GENERIC_STATUS};
    
    private static int statusCount = 0;
}

