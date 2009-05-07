package edu.sc.seis.sod;

import java.util.ArrayList;
import java.util.List;
import java.util.TimerTask;

import edu.iris.Fissures.network.ChannelImpl;
import edu.iris.Fissures.network.NetworkIdUtil;
import edu.iris.Fissures.network.StationImpl;
import edu.sc.seis.fissuresUtil.cache.CacheNetworkAccess;
import edu.sc.seis.fissuresUtil.exceptionHandler.GlobalExceptionHandler;
import edu.sc.seis.fissuresUtil.hibernate.ChannelGroup;

public class RefreshNetworkArm extends TimerTask {

    public RefreshNetworkArm(NetworkArm netArm) {
        this.netArm = netArm;
    }

    public void run() {
        logger.info("Refreshing Network Arm");
        try {
            CacheNetworkAccess[] nets;
            synchronized(this) {
                nets = netArm.getSuccessfulNetworksFromServer();
                // maybe previous update has not yet finished, only reload nets
                // not already in list???
                for (int i = 0; i < nets.length; i++) {
                    if (!isNetworkBeingReloaded(nets[i].get_attributes().getDbid())) {
                        networksBeingReloaded.add(new Integer(nets[i].get_attributes().getDbid()));
                    } else {
                        logger.info("net already in processing list, skipping..."+NetworkIdUtil.toString(nets[i].get_attributes()));
                        nets[i] = null; // skip it
                    }
                }
            }
            
            for (int i = 0; i < nets.length; i++) {
                if (nets[i] == null) {
                    continue;
                }
                processNetwork(nets[i]);
                synchronized(this) {
                    networksBeingReloaded.remove(new Integer(nets[i].get_attributes().getDbid()));
                    // in case networkArm methods are waiting on this network to be refreshed 
                    notifyAll();
                }
            }
            netArm.finish();
        } catch(Throwable t) {
            GlobalExceptionHandler.handle(t);
        }
    }

    void processNetwork(CacheNetworkAccess net) {
        try {
            logger.info("process "+NetworkIdUtil.toString(net.get_attributes()));
            StationImpl[] stas = netArm.getSuccessfulStationsFromServer(net.get_attributes());
            logger.info("found "+stas.length+" stations in "+NetworkIdUtil.toString(net.get_attributes()));
            if (Start.getWaveformRecipe() != null || netArm.getChannelSubsetters().size() != 0) {
            if (Start.getWaveformRecipe() instanceof MotionVectorArm) {
                for (int s = 0; s < stas.length; s++) {
                    List<ChannelGroup> cg = netArm.getSuccessfulChannelGroupsFromServer(stas[s]);
                }
            } else {
                for (int s = 0; s < stas.length; s++) {
                    List<ChannelImpl> chans = netArm.getSuccessfulChannelsFromServer(stas[s]);
                }
            }
            }
        } catch(Throwable t) {
            String netstr = "unknown";
            try {
                netstr = NetworkIdUtil.toString(net.get_attributes());
            } catch(Throwable tt) {}
            GlobalExceptionHandler.handle("Problem with network: " + netstr, t);
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
