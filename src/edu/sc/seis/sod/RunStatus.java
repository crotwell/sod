/**
 * RunStatus.java
 *
 * @author Created by Omnicore CodeGuide
 */

package edu.sc.seis.sod;

import org.apache.log4j.lf5.LogLevel;

public class RunStatus{
    private RunStatus(String name){
        this.name = name;
    }
    
    public static final RunStatus NEW = new RunStatus("New");
    
    public static final RunStatus PASSED = new RunStatus("Passed");
    
    public static final RunStatus FAILED = new RunStatus("Failed");
    
    public static final RunStatus GENERIC_STATUS = new RunStatus("Info");
    
    public static LogLevel[] LOG_LEVELS;
    
    public static LogLevel[] getLogLevels(){
        if(LOG_LEVELS == null){
            LogLevel[] levels = { NEW.getLogLevel(), PASSED.getLogLevel(),
                    FAILED.getLogLevel(), GENERIC_STATUS.getLogLevel()};
            LOG_LEVELS = levels;
        }
        return LOG_LEVELS;
    }
    
    public String toString(){ return name; }
    
    private String name;
    
    //Run status uses this getLogLevel method so that the LogLevel isn't created
    //unless it's accessed.  Since a log level includes a color by default, it
    //requires a GraphicsConfiguration to create, and this allows sod to run in
    //a headless system.
    public LogLevel getLogLevel(){
        if(level == null){
            level = new LogLevel(name, statusCount++);
        }
        return level;
    }
        
    private LogLevel level;
    
    private static int statusCount = 0;
}
