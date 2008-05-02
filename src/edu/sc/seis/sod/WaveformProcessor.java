package edu.sc.seis.sod;

import edu.sc.seis.sod.hibernate.SodDB;

public class WaveformProcessor extends Thread {

    public WaveformProcessor() {
        super("WaveformProcessor "+nextProcessorNum());
        setDaemon(true);
    }

    public void run() {
        while(true) {
            AbstractEventPair next = getNext(Standing.INIT);
            if(next == null) {
                // nothing to do in db, sleep a bit
                try {
                    Thread.sleep(1000);
                } catch(InterruptedException e) {}
            } else {
                processorStartWork();
                try {
                next.run();
                } catch (Throwable t) {
                    next.update(t, Status.get(Stage.EVENT_CHANNEL_POPULATION, Standing.SYSTEM_FAILURE));
                }
                SodDB.commit();
                processorFinishWork();
            }
        }
    }

    protected synchronized AbstractEventPair getNext(Standing standing) {
        AbstractEventPair out = null;
        if(seisArm != null) {
            EventChannelPair ecp = soddb.getNextECP(standing);
            ecp.update(Status.get(Stage.EVENT_CHANNEL_SUBSETTER, Standing.INIT));
            SodDB.commit();
            out = ecp;
        } else {
            EventVectorPair evp = soddb.getNextEVP(standing);
            evp.update(Status.get(Stage.EVENT_CHANNEL_SUBSETTER, Standing.INIT));
            SodDB.commit();
            out = evp;
        }
        if(out == null) {
            EventStationPair ecp = soddb.getNextESP(standing);
            ecp.update(Status.get(Stage.EVENT_STATION_SUBSETTER, Standing.INIT));
            SodDB.commit();
            out = ecp;
        }
        if(out == null) {
            EventNetworkPair ecp = soddb.getNextENP(standing);
            ecp.update(Status.get(Stage.EVENT_STATION_SUBSETTER, Standing.INIT));
            SodDB.commit();
            out = ecp;
        }
        return out;
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
    
}
