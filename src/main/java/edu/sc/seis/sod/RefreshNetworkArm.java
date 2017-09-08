package edu.sc.seis.sod;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.TimerTask;

import edu.sc.seis.seisFile.fdsnws.stationxml.Channel;
import edu.sc.seis.seisFile.fdsnws.stationxml.InvalidResponse;
import edu.sc.seis.seisFile.fdsnws.stationxml.Network;
import edu.sc.seis.seisFile.fdsnws.stationxml.Station;
import edu.sc.seis.sod.hibernate.ChannelNotFound;
import edu.sc.seis.sod.hibernate.ChannelSensitivity;
import edu.sc.seis.sod.hibernate.NetworkDB;
import edu.sc.seis.sod.model.common.QuantityImpl;
import edu.sc.seis.sod.model.station.ChannelGroup;
import edu.sc.seis.sod.model.station.ChannelIdUtil;
import edu.sc.seis.sod.model.station.NetworkIdUtil;
import edu.sc.seis.sod.source.SodSourceException;
import edu.sc.seis.sod.source.network.LoadedNetworkSource;
import edu.sc.seis.sod.util.exceptionHandler.GlobalExceptionHandler;

public class RefreshNetworkArm extends TimerTask {

    public RefreshNetworkArm(NetworkArm netArm) {
        this.netArm = netArm;
    }

    public void run() {
        logger.info("Refreshing Network Arm");
        try {
            List<Network> nets;
            List<Network> needReload = new LinkedList<Network>();
            synchronized(this) {
                nets = netArm.getSuccessfulNetworksFromServer();
                if (nets.size() == 0) {return;}
                
                // maybe previous update has not yet finished, only reload nets
                // not already in list???
                for (Network net : nets) {
                    if (!isNetworkBeingReloaded(net.getDbid())) {
                        networksBeingReloaded.add(new Integer(net.getDbid()));
                        needReload.add(net);
                        logger.debug("Will Reload "+net.toString());
                    } else {
                        logger.info("net already in processing list, skipping..."+net.toString());
                    }
                }
            }

            for (Network cacheNetwork : needReload) {
                NetworkDB.getSingleton().put(cacheNetwork);
            }
            NetworkDB.commit();
            while (needReload.size() != 0) {
                // in case of comm failures, we move to the next network, but
                // keep trying until all networks have been reloaded
                Iterator<Network> it = needReload.iterator();
                while (it.hasNext()) {
                    Network net = it.next();
                    if (processNetwork(net)) {
                        synchronized(this) {
                            networksBeingReloaded.remove(new Integer(net.getDbid()));
                            it.remove();
                            logger.debug("Successful reload of "+net.toString());
                            // in case networkArm methods are waiting on this network to be refreshed 
                            notifyAll();
                            if (Start.getWaveformRecipe() != null) {
                                // maybe worker threads need to run
                                wait(10);
                            }
                        }
                    } else {
                        logger.debug("reload not successful, will do again "+net.toString());
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

    boolean processNetwork(Network net) {
logger.debug("refresh "+net.toString());
// how do we refresh instrumentation???
        
        try {
            Station[] stas = netArm.getSuccessfulStationsFromServer(net);
            List<Station> allStations = new ArrayList<Station>();
            for (int s = 0; s < stas.length; s++) {
                allStations.add(stas[s]);
            }
            synchronized(this) {
                for (int s = 0; s < stas.length; s++) {
                    stationsBeingReloaded.add(stas[s].getDbid());
                }
            }
            logger.info("found "+stas.length+" stations in "+net.toString());
            if (Start.getWaveformRecipe() != null || netArm.getChannelSubsetters().size() != 0) {
                for (int s = 0; s < stas.length; s++) {
                    LoadedNetworkSource loadSource = new LoadedNetworkSource(netArm.getInternalNetworkSource(), allStations, stas[s]);
                    processStation(loadSource, stas[s]);
                    synchronized(this) {
                        stationsBeingReloaded.remove(new Integer(stas[s].getDbid()));
                    }
                }
            } else {
                logger.info("Not loading channels as no waveformArm or channel subsetters");
            }
            NetworkDB.commit(); // make sure session is clear
            return true;
        } catch(Throwable t) {
            NetworkDB.rollback(); // oops
            String netstr = "unknown";
            try {
                netstr = net.toString();
            } catch(Throwable tt) {}
            GlobalExceptionHandler.handle("Problem with network: " + netstr, t);
            return false;
        }
    }
    
    void processStation(LoadedNetworkSource loadSource, Station sta) {
        if (Start.getWaveformRecipe() instanceof MotionVectorArm) {
            List<ChannelGroup> cg = netArm.getSuccessfulChannelGroupsFromServer(sta, loadSource);
            // need to figure out how to update sensitivity/response, but only when it is actually used
            // as this is a time consuming task
            //   for (ChannelGroup channelGroup : cg) {
            //        checkSensitivityLoaded(channelGroup, loadSource);
            //    }
        } else {
            List<Channel> chans = netArm.getSuccessfulChannelsFromServer(sta, loadSource);
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
    
    void checkSensitivityLoaded(Channel chan, LoadedNetworkSource loadSource) {
        try {
            QuantityImpl sens = loadSource.getSensitivity(chan);
        } catch(SodSourceException e) {
            logger.warn("Error getting Instrumentation for "+ChannelIdUtil.toStringFormatDates(chan));
            NetworkDB.getSingleton().putSensitivity( ChannelSensitivity.createNonChannelSensitivity(chan));
        } catch(ChannelNotFound e) {
            logger.warn("No Instrumentation for "+ChannelIdUtil.toStringFormatDates(chan));
            NetworkDB.getSingleton().putSensitivity( ChannelSensitivity.createNonChannelSensitivity(chan));
        } catch(InvalidResponse e) {
            logger.warn("Invalid Instrumentation for "+ChannelIdUtil.toStringFormatDates(chan));
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
