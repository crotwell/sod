package edu.sc.seis.sod;

import edu.sc.seis.sod.database.Status;
import edu.sc.seis.sod.database.waveform.EventChannelCondition;
import org.apache.log4j.lf5.LogLevel;

public class RunStatus{
    private RunStatus(String name, int count){
        this.name = name;
        this.count = count;
    }
    
    public static final RunStatus NEW = new RunStatus("New", 0);
    public static final RunStatus PASSED = new RunStatus("Passed", 1);
    public static final RunStatus FAILED = new RunStatus("Failed", 2);
    public static final RunStatus SYS_FAILURE = new RunStatus("System Failure", 3);
    public static final RunStatus GENERIC = new RunStatus("Info", 4);
    public static final RunStatus PROCESSING = new RunStatus("Processing", 5);
    
    public static LogLevel[] LOG_LEVELS;
    
    public static RunStatus getStatus(String statusName){
        if(statusName.equals(NEW.toString())) return NEW;
        else if(statusName.equals(PASSED.toString())) return PASSED;
        else if(statusName.equals(FAILED.toString())) return FAILED;
        else if(statusName.equals(SYS_FAILURE.toString())) return SYS_FAILURE;
        else if(statusName.equals(GENERIC.toString())) return GENERIC;
        else if(statusName.equals(PROCESSING.toString())) return PROCESSING;
        throw new IllegalArgumentException("No such status");
    }
    
    public int getCount(){ return count; }
    
    public static RunStatus translate(Status status) {
        if(status == Status.NEW || status == Status.RE_OPEN) return NEW;
        else if(status == Status.COMPLETE_SUCCESS ||
                status == Status.RE_OPEN_SUCCESS) return PASSED;
        else if(status == Status.COMPLETE_REJECT ||
                status == Status.RE_OPEN_REJECT) return FAILED;
        else if(status == Status.PROCESSING ||
                status == Status.RE_OPEN_PROCESSING ||
                status == Status.AWAITING_FINAL_STATUS) return PROCESSING;
        else if(status == Status.SOD_FAILURE) return SYS_FAILURE;
        else return GENERIC;
    }
    
    public static RunStatus translate(EventChannelCondition cond){
        if(cond == EventChannelCondition.NEW) return NEW;
        else if(cond == EventChannelCondition.FAILURE ||
                cond == EventChannelCondition.SUBSETTER_FAILED) return FAILED;
        else return PASSED;
    }
    
    public static RunStatus getStatus(int count){
        switch(count){
            case 0: return NEW;
            case 1: return PASSED;
            case 2: return FAILED;
            case 3: return SYS_FAILURE;
            case 4: return GENERIC;
            case 5: return PROCESSING;
        }
        throw new IllegalArgumentException("No status for " + count);
    }
    
    public static LogLevel[] getLogLevels(){
        if(LOG_LEVELS == null){
            LogLevel[] levels = { NEW.getLogLevel(), PASSED.getLogLevel(),
                    FAILED.getLogLevel(), GENERIC.getLogLevel()};
            LOG_LEVELS = levels;
        }
        return LOG_LEVELS;
    }
    
    public String toString(){ return name; }
    
    private String name;
    
    private int count;
    
    //Run status uses this getLogLevel method so that the LogLevel isn't created
    //unless it's accessed.  Since a log level includes a color by default, it
    //requires a GraphicsConfiguration to create, and this allows sod to run in
    //a headless system.
    public LogLevel getLogLevel(){
        if(level == null) level = new LogLevel(name, statusCount++);
        return level;
    }
    
    private LogLevel level;
    
    private static int statusCount = 0;
}
