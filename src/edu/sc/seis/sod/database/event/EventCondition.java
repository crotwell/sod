/**
 * EventStatus.java
 *
 * @author Created by Omnicore CodeGuide
 */

package edu.sc.seis.sod.database.event;

import org.apache.log4j.lf5.LogLevel;

public class EventCondition{
    private EventCondition(int number, String name){
        this.number = number;
        this.name = name;
    }
    
    public int getNumber(){ return number; }
    
    public String getName(){ return name; }
    
    public static EventCondition getByNumber(int number){
        for (int i = 0; i < statii.length; i++) {
            if(statii[i].getNumber() == number) return statii[i];
        }
        throw new IllegalArgumentException("No status with index " + number);
    }
    
    public static EventCondition getByName(String name){
        for (int i = 0; i < statii.length; i++) {
            if(statii[i].getName().equals(name)) return statii[i];
        }
        throw new IllegalArgumentException("No status with name " + name);
    }
    
    public static final EventCondition NEW = new EventCondition(0, "New");
    public static final EventCondition SUBSETTER_PASSED = new EventCondition(1, "EventSubsetterPassed");
    public static final EventCondition SUBSETTER_FAILED = new EventCondition(2, "EventSubsetterFailed");
    public static final EventCondition PROCESSOR_FAILED = new EventCondition(3, "EventProcessorFailed");
    public static final EventCondition PROCESSOR_PASSED = new EventCondition(4, "EventProcessorComplete");
    public static final EventCondition PAIRING_FAILED = new EventCondition(5, "WaveformPairingFailed");
    public static final EventCondition SUCCESS = new EventCondition(6, "Success");
    public static final EventCondition FAILURE = new EventCondition(7, "Failed");
    
    
    public static final EventCondition[] statii = { NEW, SUBSETTER_PASSED,
            SUBSETTER_FAILED, PROCESSOR_FAILED, PROCESSOR_PASSED,
            PAIRING_FAILED, SUCCESS, FAILURE};
    
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

