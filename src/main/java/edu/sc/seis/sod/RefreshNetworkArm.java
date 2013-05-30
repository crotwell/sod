package edu.sc.seis.sod;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.TimerTask;

import edu.iris.Fissures.IfNetwork.ChannelNotFound;
import edu.iris.Fissures.model.QuantityImpl;
import edu.iris.Fissures.network.ChannelIdUtil;
import edu.iris.Fissures.network.ChannelImpl;
import edu.iris.Fissures.network.NetworkAttrImpl;
import edu.iris.Fissures.network.NetworkIdUtil;
import edu.iris.Fissures.network.StationImpl;
import edu.sc.seis.fissuresUtil.exceptionHandler.GlobalExceptionHandler;
import edu.sc.seis.fissuresUtil.hibernate.ChannelGroup;
import edu.sc.seis.fissuresUtil.hibernate.ChannelSensitivity;
import edu.sc.seis.fissuresUtil.hibernate.InstrumentationBlob;
import edu.sc.seis.fissuresUtil.hibernate.NetworkDB;
import edu.sc.seis.fissuresUtil.sac.InvalidResponse;
import edu.sc.seis.sod.source.network.LoadedNetworkSource;
import edu.sc.seis.sod.source.network.NetworkFinder;

public class RefreshNetworkArm extends TimerTask {

    public RefreshNetworkArm(NetworkArm netArm) {
        this.netArm = netArm;
        InstrumentationBlob.setORB(CommonAccess.getORB());
    }

    public void run() {
        logger.info("Refreshing Network Arm");
        try {
            List<NetworkAttrImpl> nets;
            List<NetworkAttrImpl> needReload = new LinkedList<NetworkAttrImpl>();
            synchronized(this) {
                if (netArm.getInternalNetworkSource() instanceof NetworkFinder) {
                    ((NetworkFinder)netArm.getInternalNetworkSource()).reset();
                }
                nets = netArm.getSuccessfulNetworksFromServer();
                // maybe previous update has not yet finished, only reload nets
                // not already in list???
                for (NetworkAttrImpl net : nets) {
                    if (!isNetworkBeingReloaded(net.getDbid())) {
                        networksBeingReloaded.add(new Integer(net.getDbid()));
                        needReload.add(net);
                        logger.debug("Will Reload "+NetworkIdUtil.toString(net));
                    } else {
                        logger.info("net already in processing list, skipping..."+NetworkIdUtil.toString(net));
                    }
                }
            }

            for (NetworkAttrImpl cacheNetwork : needReload) {
                NetworkDB.getSingleton().put(cacheNetwork);
            }
            NetworkDB.commit();
            while (needReload.size() != 0) {
                // in case of comm failures, we move to the next network, but
                // keep trying until all networks have been reloaded
                Iterator<NetworkAttrImpl> it = needReload.iterator();
                while (it.hasNext()) {
                    NetworkAttrImpl net = it.next();
                    if (processNetwork(net)) {
                        synchronized(this) {
                            networksBeingReloaded.remove(new Integer(net.getDbid()));
                            it.remove();
                            logger.debug("Successful reload of "+NetworkIdUtil.toStringNoDates(net));
                            // in case networkArm methods are waiting on this network to be refreshed 
                            notifyAll();
                            if (Start.getWaveformRecipe() != null) {
                                // maybe worker threads need to run
                                wait(10);
                            }
                        }
                    } else {
                        logger.debug("reload not successful, will do again "+NetworkIdUtil.toStringNoDates(net));
                    }
                }
                Thread.sleep(1000); // don't retry over and over as fast as possible
            }
            netArm.finish();
        } catch(Throwable t) {
            Start.armFailure(netArm, t);
            GlobalExceptionHandler.handle(t);
        }
    }

    boolean processNetwork(NetworkAttrImpl net) {
logger.debug("refresh "+NetworkIdUtil.toString(net));
// how do we refresh instrumentation???
        
        try {
            StationImpl[] stas = netArm.getSuccessfulStationsFromServer(net);
            List<StationImpl> allStations = new ArrayList<StationImpl>();
            for (int s = 0; s < stas.length; s++) {
                allStations.add(stas[s]);
            }
            synchronized(this) {
                for (int s = 0; s < stas.length; s++) {
                    stationsBeingReloaded.add(stas[s].getDbid());
                }
            }
            logger.info("found "+stas.length+" stations in "+NetworkIdUtil.toString(net));
            if (Start.getWaveformRecipe() != null || netArm.getChannelSubsetters().size() != 0) {
                for (int s = 0; s < stas.length; s++) {
                    LoadedNetworkSource loadSource = new LoadedNetworkSource(netArm.getInternalNetworkSource(), allStations, stas[s]);
                    processStation(loadSource, stas[s]);
                    synchronized(this) {
                        stationsBeingReloaded.remove(new Integer(stas[s].getDbid()));
                    }
                }
            }
            NetworkDB.commit(); // make sure session is clear
            return true;
        } catch(Throwable t) {
            NetworkDB.rollback(); // oops
            String netstr = "unknown";
            try {
                netstr = NetworkIdUtil.toString(net);
            } catch(Throwable tt) {}
            GlobalExceptionHandler.handle("Problem with network: " + netstr, t);
            return false;
        }
    }
    
    void processStation(LoadedNetworkSource loadSource, StationImpl sta) {
        if (Start.getWaveformRecipe() instanceof MotionVectorArm) {
            List<ChannelGroup> cg = netArm.getSuccessfulChannelGroupsFromServer(sta, loadSource);
            // need to figure out how to update sensitivity/response, but only when it is actually used
            // as this is a time consuming task
            //   for (ChannelGroup channelGroup : cg) {
            //        checkSensitivityLoaded(channelGroup, loadSource);
            //    }
        } else {
            List<ChannelImpl> chans = netArm.getSuccessfulChannelsFromServer(sta, loadSource);
            //    for (ChannelImpl channelImpl : chans) {
            //        checkSensitivityLoaded(channelImpl, loadSource);
            //    }
        }
    }
    
    void checkSensitivityLoaded(ChannelGroup cg, LoadedNetworkSource loadSource) {
        checkSensitivityLoaded(cg.getChannel1(), loadSource);
        checkSensitivityLoaded(cg.getChannel2(), loadSource);
        checkSensitivityLoaded(cg.getChannel3(), loadSource);
    }
    
    void checkSensitivityLoaded(ChannelImpl chan, LoadedNetworkSource loadSource) {
        try {
            QuantityImpl sens = loadSource.getSensitivity(chan.getId());
        } catch(ChannelNotFound e) {
            logger.warn("No Instrumentation for "+ChannelIdUtil.toStringFormatDates(chan.getId()));
            NetworkDB.getSingleton().putSensitivity( ChannelSensitivity.createNonChannelSensitivity(chan));
        } catch(InvalidResponse e) {
            logger.warn("Invalid Instrumentation for "+ChannelIdUtil.toStringFormatDates(chan.getId()));
            NetworkDB.getSingleton().putSensitivity( ChannelSensitivity.createNonChannelSensitivity(chan));
        }
    }

    public synchronized boolean isNetworkBeingReloaded(int dbid) {
        if (dbid == 0) {
            throw new IllegalArgumentException("dbid = 0 is not legal, Network must not be in db yet");
        }
        return networksBeingReloaded.contains(new Integer(dbid));
    }

    public synchronized boolean isStationBeingReloaded(int dbid) {
        if (dbid == 0) {
            throw new IllegalArgumentException("dbid = 0 is not legal, Station must not be in db yet");
        }
        return stationsBeingReloaded.contains(new Integer(dbid));
    }

    private List<Integer> networksBeingReloaded = new ArrayList<Integer>();
    
    private List<Integer> stationsBeingReloaded = new ArrayList<Integer>();

    NetworkArm netArm;

    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(RefreshNetworkArm.class);
}
