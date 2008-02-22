package edu.sc.seis.sod;

import edu.iris.Fissures.IfNetwork.Station;
import edu.sc.seis.fissuresUtil.exceptionHandler.GlobalExceptionHandler;
import edu.sc.seis.sod.hibernate.SodDB;
import edu.sc.seis.sod.status.StringTree;
import edu.sc.seis.sod.status.StringTreeLeaf;


public class MotionVectorWaveformWorkUnit extends WaveformWorkUnit {

    /** for hibernate */
    protected MotionVectorWaveformWorkUnit() {}
    
    public MotionVectorWaveformWorkUnit(EventVectorPair pair) {
        this.evp = pair;
    }

    public void run() {
        try {
            //reattach evp to this session
            evp = (EventVectorPair)sodDb.getSession().load(EventVectorPair.class, new Integer(evp.getPairId()));
            logger.debug("Begin work on EVP: "+evp.getEcp1());
            evp.update(Status.get(Stage.EVENT_STATION_SUBSETTER,
                                  Standing.IN_PROG));
            StringTree accepted = new StringTreeLeaf(this, false);
            try {
                Station evStation = evp.getChannelGroup().getChannels()[0].my_site.my_station;
                synchronized(Start.getWaveformArm().getEventStationSubsetter()) {
                    accepted = Start.getWaveformArm().getEventStationSubsetter().accept(evp.getEvent(),
                                                            evStation,
                                                            evp.getCookieJar());
                }
            } catch(Throwable e) {
                evp.update(e, Status.get(Stage.EVENT_STATION_SUBSETTER,
                                         Standing.SYSTEM_FAILURE));
                failLogger.warn(evp, e);
                SodDB.commit();
                logger.debug("Finish (fail) EVP: "+evp.getEcp1());
                return;
            }
            if(accepted.isSuccess()) {
                Start.getWaveformArm().getMotionVectorArm().processMotionVectorArm(evp);
            } else {
                evp.update(Status.get(Stage.EVENT_STATION_SUBSETTER,
                                      Standing.REJECT));
                failLogger.info(evp + "  " + accepted.toString());
            }
            Status stat = evp.getStatus();
            // Only make one retry for the whole vector
            if(stat.getStanding() == Standing.CORBA_FAILURE) {
                sodDb.retry(this);
            } else if(stat.getStanding() == Standing.RETRY) {
                sodDb.retry(this);
            }
            SodDB.getSession().saveOrUpdate(evp);
            SodDB.commit();
            logger.debug("Finished with EVP: "+evp.getEcp1());
        } catch(Throwable t) {
            System.err.println(WaveformArm.BIG_ERROR_MSG);
            t.printStackTrace(System.err);
            GlobalExceptionHandler.handle(WaveformArm.BIG_ERROR_MSG, t);
            SodDB.rollback();
        }
    }

    public String toString() {
        StringBuffer buff = new StringBuffer("MotionVectorWorkUnit(");
        for(int i = 0; i < evp.pairs.length; i++) {
            buff.append(evp.pairs[i]);
            buff.append(',');
        }
        return buff.toString();
    }
    
    public EventVectorPair getEvp() {
    	return evp;
    }
    
    // for hibernate
    protected void setEvp(EventVectorPair evp) {
        this.evp = evp;
    }
    
    EventVectorPair evp;
    
    private static final org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(MotionVectorWaveformWorkUnit.class);
}