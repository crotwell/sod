package edu.sc.seis.sod.database.waveform;

import org.apache.log4j.lf5.LogLevel;

public class EventChannelCondition{

    private EventChannelCondition(int number, String name){
        this.number = number;
        this.name = name;
    }
    
    public int getNumber(){ return number; }
    
    public String getName(){ return name; }
    
    public static EventChannelCondition getByNumber(int number){
        for (int i = 0; i < statii.length; i++) {
            if(statii[i].getNumber() == number) return statii[i];
        }
        throw new IllegalArgumentException("No status with index " + number);
    }
    
    public static EventChannelCondition getByName(String name){
        for (int i = 0; i < statii.length; i++) {
            if(statii[i].getName().equals(name)) return statii[i];
        }
        throw new IllegalArgumentException("No status with name " + name);
    }
    
    public static final EventChannelCondition NEW = new EventChannelCondition(0, "New");
    public static final EventChannelCondition SUBSETTER_PASSED = new EventChannelCondition(1, "SubsetterPassed");
    public static final EventChannelCondition SUBSETTER_FAILED = new EventChannelCondition(2, "SubsetterFailed");
    public static final EventChannelCondition PROCESSOR_FAILED = new EventChannelCondition(3, "ProcessorFailed");
    public static final EventChannelCondition PROCESSOR_PASSED = new EventChannelCondition(4, "ProcessorComplete");
    public static final EventChannelCondition SUCCESS = new EventChannelCondition(5, "Success");
    public static final EventChannelCondition FAILURE = new EventChannelCondition(6, "Failed");
    public static final EventChannelCondition SUBSETTER_STARTED = new EventChannelCondition(7, "SubsetterStarted");
    public static final EventChannelCondition RETRY = new EventChannelCondition(8, "Retry");
    
    
    public static final EventChannelCondition[] statii = { NEW, SUBSETTER_PASSED,
            SUBSETTER_FAILED, PROCESSOR_FAILED, PROCESSOR_PASSED, SUCCESS,
            FAILURE};
    
    public String toString(){ return name; }
    
    //Run status uses this getLogLevel method so that the LogLevel isn't created
    //unless it's accessed.  Since a log level includes a color by default, it
    //requires a GraphicsConfiguration to create, and this allows sod to run in
    //a headless system.
    public LogLevel getLogLevel(){
        if(level == null) level = new LogLevel(name, number);
        return level;
    }
    
    private LogLevel level;
    
    private int number;
    
    private String name;

}

