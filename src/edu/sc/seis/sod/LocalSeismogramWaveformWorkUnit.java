package edu.sc.seis.sod;

import edu.iris.Fissures.IfNetwork.Station;
import edu.sc.seis.fissuresUtil.exceptionHandler.GlobalExceptionHandler;
import edu.sc.seis.sod.hibernate.SodDB;
import edu.sc.seis.sod.status.StringTree;
import edu.sc.seis.sod.status.StringTreeLeaf;


public class LocalSeismogramWaveformWorkUnit extends WaveformWorkUnit {

    /** for hibernate */
    protected LocalSeismogramWaveformWorkUnit() {}

    public LocalSeismogramWaveformWorkUnit(EventChannelPair ecp) {
        this.ecp = ecp;
    }

    public void run() {
        try {
            //reattach ecp to this session
            ecp = (EventChannelPair)sodDb.getSession().load(EventChannelPair.class, new Integer(ecp.getPairId()));
            ecp.update(Status.get(Stage.EVENT_STATION_SUBSETTER,
                                  Standing.IN_PROG));
            StringTree accepted = new StringTreeLeaf(this, false);
            try {
                Station evStation = ecp.getChannel().getSite().getStation();
                synchronized(Start.getWaveformArm().getEventStationSubsetter()) {
                    accepted = Start.getWaveformArm().getEventStationSubsetter().accept(ecp.getEvent(),
                                                            evStation,
                                                            ecp.getCookieJar());
                }
            } catch(Throwable e) {
                if(e instanceof org.omg.CORBA.SystemException) {
                    ecp.update(e, Status.get(Stage.EVENT_STATION_SUBSETTER, Standing.CORBA_FAILURE));
                    sodDb.retry(this);
                    failLogger.info("Network or server problem, SOD will continue to retry this item periodically: ("+e.getClass().getName()+") "+ecp);
                    logger.debug(ecp, e);
                } else {
                    ecp.update(e, Status.get(Stage.EVENT_STATION_SUBSETTER,
                                             Standing.SYSTEM_FAILURE));
                    failLogger.warn(ecp, e);
                }
                SodDB.commit();
                return;
            }
            if(accepted.isSuccess()) {
                Start.getWaveformArm().getLocalSeismogramArm().processLocalSeismogramArm(ecp);
            } else {
                ecp.update(Status.get(Stage.EVENT_STATION_SUBSETTER,
                                      Standing.REJECT));
                failLogger.info(ecp + "  " + accepted.toString());
            }
            Status stat = ecp.getStatus();
            if(stat.getStanding() == Standing.CORBA_FAILURE) {
                sodDb.retry(this);
            } else if(stat.getStanding() == Standing.RETRY) {
                sodDb.retry(this);
            }
            SodDB.commit();
        } catch(Throwable t) {
            System.err.println(WaveformArm.BIG_ERROR_MSG);
            t.printStackTrace(System.err);
            GlobalExceptionHandler.handle(WaveformArm.BIG_ERROR_MSG, t);
        }
    }

    public String toString() {
        return "LocalSeismogramWorkUnit(" + ecp + ")";
    }
    
    public EventChannelPair getEcp() {
    	return ecp;
    }
    
    //hibernate
    protected void setEcp(EventChannelPair p) {
        this.ecp = p;
    }

    public boolean equals(Object o) {
        return super.equals(o) && o instanceof LocalSeismogramWaveformWorkUnit && ecp.equals(((LocalSeismogramWaveformWorkUnit)o).getEcp());
    }
    
    public int hashCode() {
        return super.hashCode()+17*getEcp().hashCode();
    }
    
    protected EventChannelPair ecp;

    private static final org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(LocalSeismogramWaveformWorkUnit.class);

}