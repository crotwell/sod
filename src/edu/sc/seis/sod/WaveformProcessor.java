package edu.sc.seis.sod;

import edu.sc.seis.sod.hibernate.SodDB;

public class WaveformProcessor extends Thread {

    public WaveformProcessor() {
        super("WaveformProcessor " + nextProcessorNum());
    }

    public void run() {
        while(true) {
            AbstractEventPair next = getNext(Standing.INIT);
            while( next == null && ( Start.getWaveformArm().possibleToContinue()
                    || SodDB.getSingleton().getNumWorkUnits(Standing.RETRY) != 0
                    || SodDB.getSingleton().getNumWorkUnits(Standing.IN_PROG) != 0 )) {
                    logger.debug("Processor waiting for work unit to show up");
                    try {
                        synchronized(this) {
                            wait(100000);
                        }
                    } catch(InterruptedException e) {}
                    next = getNext(Standing.INIT);
            }
            if(next != null) {
                processorStartWork();
                try {
                    next.run();
                } catch(Throwable t) {
                    next.update(t, Status.get(Stage.EVENT_CHANNEL_POPULATION,
                                              Standing.SYSTEM_FAILURE));
                }
                SodDB.commit();
                processorFinishWork();
            } else {
                // nothing to do in db, not possible to continue
                logger.debug("No work to do, quiting processing");
                return;
            }
        }
    }

    protected synchronized AbstractEventPair getNext(Standing standing) {
        AbstractEventPair out = null;
        double retryRandom = Math.random();
        if(seisArm != null) {
            EventChannelPair ecp = null;
            if (retryRandom < Start.getWaveformArm().getRetryPercentage()) {
                // try a retry
                ecp = soddb.getNextRetryECP();
            } 
            if (ecp == null) {
                ecp = soddb.getNextECP(standing); 
            }
            
            if(ecp != null) {
                ecp.update(Status.get(Stage.EVENT_CHANNEL_SUBSETTER,
                                      Standing.INIT));
                SodDB.commit();
                return ecp;
            }
        } else {
            EventVectorPair evp = null;
            if (retryRandom < Start.getWaveformArm().getRetryPercentage()) {
                // try a retry
                evp = soddb.getNextRetryEVP();
            } 
            if (evp == null) {
                evp = soddb.getNextEVP(standing);
            }
            
            if(evp != null) {
                evp.update(Status.get(Stage.EVENT_CHANNEL_SUBSETTER,
                                      Standing.INIT));
                SodDB.commit();
                return evp;
            }
        }
        // no ecp/evp try e-station
        EventStationPair esp = soddb.getNextESP(standing);
        if(esp != null) {
            esp.update(Status.get(Stage.EVENT_STATION_SUBSETTER, Standing.INIT));
            SodDB.commit();
            return esp;
        }
        // no e-station try e-network
        EventNetworkPair enp = soddb.getNextENP(standing);
        if(enp != null) {
            enp.update(Status.get(Stage.EVENT_STATION_SUBSETTER, Standing.INIT));
            SodDB.commit();
            return enp;
        }
        return null;
    }

    SodDB soddb = SodDB.getSingleton();

    LocalSeismogramArm seisArm = Start.getWaveformArm().getLocalSeismogramArm();

    MotionVectorArm vectorArm = Start.getWaveformArm().getMotionVectorArm();

    public static int getProcessorsWorking() {
        return processorsWorking;
    }

    static int processorsWorking = 0;

    static void processorStartWork() {
        processorsWorking++;
    }

    static void processorFinishWork() {
        processorsWorking--;
    }

    static int usedProcessorNum = 0;

    static int nextProcessorNum() {
        return usedProcessorNum++;
    }

    private static final org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(WaveformProcessor.class);
}
