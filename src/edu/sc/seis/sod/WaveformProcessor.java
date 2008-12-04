package edu.sc.seis.sod;

import edu.sc.seis.fissuresUtil.exceptionHandler.GlobalExceptionHandler;
import edu.sc.seis.sod.hibernate.SodDB;

public class WaveformProcessor extends Thread {

    public WaveformProcessor() {
        super("WaveformProcessor " + nextProcessorNum());
    }

    public void run() {
        try {
            while(true) {
                AbstractEventPair next = getNext(Standing.INIT);
                while(next == null
                        && (Start.getWaveformArm().possibleToContinue()
                                || SodDB.getSingleton().getNumWorkUnits(Standing.RETRY) != 0 || SodDB.getSingleton()
                                .getNumWorkUnits(Standing.IN_PROG) != 0)) {
                    logger.debug("Processor waiting for work unit to show up");
                    try {
                        // sleep, but wake up if waveformArm does notifyAll()
                        logger.debug("waiting on waveform arm");
                        synchronized(Start.getWaveformArm().getWaveformProcessorSync()) {
                            Start.getWaveformArm().getWaveformProcessorSync().notifyAll();
                            if(Start.getWaveformArm().possibleToContinue()) {
                                Start.getWaveformArm().getWaveformProcessorSync().wait();
                            }
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
                        SodDB.rollback();
                        next.update(t, Status.get(Stage.EVENT_CHANNEL_POPULATION, Standing.SYSTEM_FAILURE));
                    }
                    SodDB.commit();
                    processorFinishWork();
                } else {
                    // nothing to do in db, not possible to continue
                    logger.debug("No work to do, quiting processing: " + Start.getWaveformArm().possibleToContinue()
                            + " " + (SodDB.getSingleton().getNumWorkUnits(Standing.RETRY) != 0) + " "
                            + (SodDB.getSingleton().getNumWorkUnits(Standing.IN_PROG) != 0));
                    return;
                }
            }
        } catch(Throwable t) {
            // just in case...
            GlobalExceptionHandler.handle(t);
        }
    }

    protected static synchronized AbstractEventPair getNext(Standing standing) {
        double retryRandom = Math.random();
        AbstractEventChannelPair ecp = null;
        if(retryRandom < Start.getWaveformArm().getRetryPercentage()) {
            // try a retry
            ecp = SodDB.getSingleton().getNextRetryECP();
        }
        if(ecp == null) {
            ecp = SodDB.getSingleton().getNextECP(standing);
        }
        if(ecp != null) {
            ecp.update(Status.get(Stage.EVENT_CHANNEL_SUBSETTER, Standing.INIT));
            SodDB.commit();
            SodDB.getSession().update(ecp);
            return ecp;
        }
        // no ecp/evp try e-station
        EventStationPair esp = SodDB.getSingleton().getNextESP(standing);
        if(esp != null) {
            esp.update(Status.get(Stage.EVENT_STATION_SUBSETTER, Standing.INIT));
            SodDB.commit();
            SodDB.getSession().update(esp);
            return esp;
        }
        // no e-station try e-network
        EventNetworkPair enp = SodDB.getSingleton().getNextENP(standing);
        if(enp != null) {
            enp.update(Status.get(Stage.EVENT_STATION_SUBSETTER, Standing.INIT));
            SodDB.commit();
            // reattach to new session
            SodDB.getSession().update(enp);
            return enp;
        }
        // go get more events to make e-net pairs
        int numEvents = Start.getWaveformArm().populateEventChannelDb(Standing.INIT);
        if(numEvents != 0) {
            enp = SodDB.getSingleton().getNextENP(standing);
            if(enp != null) {
                enp.update(Status.get(Stage.EVENT_STATION_SUBSETTER, Standing.INIT));
                SodDB.commit();
                // reattach to new session
                SodDB.getSession().update(enp);
                return enp;
            }
        }
        // really nothing doing, might as well work on a retry?
        return SodDB.getSingleton().getNextRetryECP();
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
