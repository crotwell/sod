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
                        // sleep, but wake up if waveformArm does notifyAll()
                        logger.debug("waiting on waveform arm");
                        synchronized(Start.getWaveformArm().getWaveformProcessorSync()) {
                            Start.getWaveformArm().getWaveformProcessorSync().wait(100000);
                        }
                    } catch(InterruptedException e) {}
                    logger.debug("done waiting on waveform arm");
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

    protected static synchronized AbstractEventPair getNext(Standing standing) {
        AbstractEventPair out = null;
        double retryRandom = Math.random();
        if(Start.getWaveformArm().getLocalSeismogramArm() != null) {
            EventChannelPair ecp = null;
            if (retryRandom < Start.getWaveformArm().getRetryPercentage()) {
                // try a retry
                ecp = SodDB.getSingleton().getNextRetryECP();
            } 
            if (ecp == null) {
                ecp = SodDB.getSingleton().getNextECP(standing); 
            }
            
            if(ecp != null) {
                ecp.update(Status.get(Stage.EVENT_CHANNEL_SUBSETTER,
                                      Standing.INIT));
                SodDB.commit();
                SodDB.getSession().refresh(ecp);
                return ecp;
            }
        } else {
            EventVectorPair evp = null;
            if (retryRandom < Start.getWaveformArm().getRetryPercentage()) {
                // try a retry
                evp = SodDB.getSingleton().getNextRetryEVP();
            } 
            if (evp == null) {
                evp = SodDB.getSingleton().getNextEVP(standing);
            }
            
            if(evp != null) {
                evp.update(Status.get(Stage.EVENT_CHANNEL_SUBSETTER,
                                      Standing.INIT));
                SodDB.commit();
                SodDB.getSession().refresh(evp);
                return evp;
            }
        }
        // no ecp/evp try e-station
        EventStationPair esp = SodDB.getSingleton().getNextESP(standing);
        if(esp != null) {
            esp.update(Status.get(Stage.EVENT_STATION_SUBSETTER, Standing.INIT));
            SodDB.commit();
            SodDB.getSession().refresh(esp);
            return esp;
        }
        // no e-station try e-network
        EventNetworkPair enp = SodDB.getSingleton().getNextENP(standing);
        if(enp != null) {
            enp.update(Status.get(Stage.EVENT_STATION_SUBSETTER, Standing.INIT));
            SodDB.commit();
            // reattach to new session
            SodDB.getSession().refresh(enp);
            return enp;
        }
        return null;
    }

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
