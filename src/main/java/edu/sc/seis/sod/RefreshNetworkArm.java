package edu.sc.seis.sod;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.TimerTask;

import edu.iris.Fissures.IfNetwork.ChannelNotFound;
import edu.iris.Fissures.IfNetwork.Instrumentation;
import edu.iris.Fissures.network.ChannelImpl;
import edu.iris.Fissures.network.NetworkIdUtil;
import edu.iris.Fissures.network.StationImpl;
import edu.sc.seis.fissuresUtil.cache.CacheNetworkAccess;
import edu.sc.seis.fissuresUtil.exceptionHandler.GlobalExceptionHandler;
import edu.sc.seis.fissuresUtil.hibernate.ChannelGroup;
import edu.sc.seis.fissuresUtil.hibernate.NetworkDB;
import edu.sc.seis.fissuresUtil.sac.InvalidResponse;
import edu.sc.seis.sod.source.network.LoadedNetworkSource;

public class RefreshNetworkArm extends TimerTask {

    public RefreshNetworkArm(NetworkArm netArm) {
        this.netArm = netArm;
    }

    public void run() {
        logger.info("Refreshing Network Arm");
        try {
            List<CacheNetworkAccess> nets;
            List<CacheNetworkAccess> needReload = new LinkedList<CacheNetworkAccess>();
            synchronized(this) {
                nets = netArm.getSuccessfulNetworksFromServer();
                // maybe previous update has not yet finished, only reload nets
                // not already in list???
                for (CacheNetworkAccess net : nets) {
                    if (!isNetworkBeingReloaded(net.get_attributes().getDbid())) {
                        networksBeingReloaded.add(new Integer(net.get_attributes().getDbid()));
                        needReload.add(net);
                    } else {
                        logger.info("net already in processing list, skipping..."+NetworkIdUtil.toString(net.get_attributes()));
                    }
                }
            }

            for (CacheNetworkAccess cacheNetworkAccess : needReload) {
                NetworkDB.getSingleton().put(cacheNetworkAccess.get_attributes());
            }
            NetworkDB.commit();
            for (CacheNetworkAccess cacheNetworkAccess : needReload) {
                processNetwork(cacheNetworkAccess);
                synchronized(this) {
                    networksBeingReloaded.remove(new Integer(cacheNetworkAccess.get_attributes().getDbid()));
                    // in case networkArm methods are waiting on this network to be refreshed 
                    notifyAll();
                    if (Start.getWaveformRecipe() != null) {
                        // maybe worker threads need to run
                        wait(10);
                    }
                }
            }
            netArm.finish();
        } catch(Throwable t) {
            GlobalExceptionHandler.handle(t);
        }
    }

    void processNetwork(CacheNetworkAccess net) {

        // how do we refresh instrumentation???
        
        try {
            StationImpl[] stas = netArm.getSuccessfulStationsFromServer(net);
            logger.info("found "+stas.length+" stations in "+NetworkIdUtil.toString(net.get_attributes()));
            if (Start.getWaveformRecipe() != null || netArm.getChannelSubsetters().size() != 0) {
                for (int s = 0; s < stas.length; s++) {
                    LoadedNetworkSource loadSource = new LoadedNetworkSource(netArm.getInternalNetworkSource(), stas[s]);
                    if (Start.getWaveformRecipe() instanceof MotionVectorArm) {
                        List<ChannelGroup> cg = netArm.getSuccessfulChannelGroupsFromServer(stas[s], loadSource);
                        for (ChannelGroup channelGroup : cg) {
                            checkInstLoaded(channelGroup, loadSource);
                        }
                    } else {
                        List<ChannelImpl> chans = netArm.getSuccessfulChannelsFromServer(stas[s], loadSource);
                        for (ChannelImpl channelImpl : chans) {
                            checkInstLoaded(channelImpl, loadSource);
                        }
                    }
                }
            }
            NetworkDB.commit(); // make sure session is clear
        } catch(Throwable t) {
            NetworkDB.rollback(); // oops
            String netstr = "unknown";
            try {
                netstr = NetworkIdUtil.toString(net.get_attributes());
            } catch(Throwable tt) {}
            GlobalExceptionHandler.handle("Problem with network: " + netstr, t);
        }
    }
    
    void checkInstLoaded(ChannelGroup cg, LoadedNetworkSource loadSource) {
        checkInstLoaded(cg.getChannel1(), loadSource);
        checkInstLoaded(cg.getChannel2(), loadSource);
        checkInstLoaded(cg.getChannel3(), loadSource);
    }
    
    void checkInstLoaded(ChannelImpl chan, LoadedNetworkSource loadSource) {
        if (loadSource.isInstrumentationLoaded(chan.getId())) {
            Instrumentation inst;
            try {
                inst = loadSource.getInstrumentation(chan.getId());
                NetworkDB.getSingleton().putInstrumentation(chan, inst);
            } catch(ChannelNotFound e) {
                logger.warn(e);
                NetworkDB.getSingleton().putInstrumentation(chan, null);
            } catch(InvalidResponse e) {
                logger.warn(e);
                NetworkDB.getSingleton().putInstrumentation(chan, null);
            }
        }
    }

    public synchronized boolean isNetworkBeingReloaded(int dbid) {
        if (dbid == 0) {
            throw new IllegalArgumentException("dbid = 0 is not legal, Network must not be in db yet");
        }
        return networksBeingReloaded.contains(new Integer(dbid));
    }

    private List<Integer> networksBeingReloaded = new ArrayList<Integer>();

    NetworkArm netArm;

    private static final org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(RefreshNetworkArm.class);
}
