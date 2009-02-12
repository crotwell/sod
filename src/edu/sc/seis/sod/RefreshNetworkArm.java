package edu.sc.seis.sod;

import java.util.List;

import edu.iris.Fissures.network.ChannelImpl;
import edu.iris.Fissures.network.StationImpl;
import edu.sc.seis.fissuresUtil.cache.CacheNetworkAccess;
import edu.sc.seis.fissuresUtil.hibernate.ChannelGroup;


public class RefreshNetworkArm implements Runnable {

    public RefreshNetworkArm(NetworkArm netArm) {
        this.netArm = netArm;
    }
    
    public void run() {
        CacheNetworkAccess[] nets = netArm.getSuccessfulNetworksFromServer();
        for (int i = 0; i < nets.length; i++) {
            StationImpl[] stas = netArm.getSuccessfulStationsFromServer(nets[i].get_attributes());
            if(Start.getWaveformRecipe() instanceof MotionVectorArm) {
                for (int s = 0; s < stas.length; s++) {
                    List<ChannelGroup> cg = netArm.getSuccessfulChannelGroupsFromServer(stas[s]);
                }
            } else {
                for (int s = 0; s < stas.length; s++) {
                    List<ChannelImpl> chans = netArm.getSuccessfulChannelsFromServer(stas[s]);
                }
            }
        }
    }
    
    NetworkArm netArm;
}
