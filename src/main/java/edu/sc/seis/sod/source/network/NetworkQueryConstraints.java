package edu.sc.seis.sod.source.network;

import java.util.ArrayList;
import java.util.List;

import edu.sc.seis.sod.subsetter.Subsetter;
import edu.sc.seis.sod.subsetter.channel.ChannelCode;
import edu.sc.seis.sod.subsetter.channel.ChannelOR;
import edu.sc.seis.sod.subsetter.channel.ChannelSubsetter;
import edu.sc.seis.sod.subsetter.channel.SiteCode;
import edu.sc.seis.sod.subsetter.network.NetworkCode;
import edu.sc.seis.sod.subsetter.network.NetworkOR;
import edu.sc.seis.sod.subsetter.network.NetworkSubsetter;
import edu.sc.seis.sod.subsetter.station.StationCode;
import edu.sc.seis.sod.subsetter.station.StationOR;
import edu.sc.seis.sod.subsetter.station.StationSubsetter;


public class NetworkQueryConstraints {

    public NetworkQueryConstraints(List<String> constrainingNetworkCodes,
                                   List<String> constrainingStationCodes,
                                   List<String> constrainingLocationCodes,
                                   List<String> constrainingChannelCodes) {
        this.constrainingNetworkCodes = constrainingNetworkCodes;
        this.constrainingStationCodes = constrainingStationCodes;
        this.constrainingLocationCodes = constrainingLocationCodes;
        this.constrainingChannelCodes = constrainingChannelCodes;
    }
    

    /**
     * Given a network subsetter, return a string array consisting of all the
     * network codes this subsetter accepts. If it doesn't constrain network
     * based only on codes possibly in an OR, an empty array is returned.
     */
    public NetworkQueryConstraints(NetworkSubsetter attrSubsetter,
                                   StationSubsetter stationSubsetter,
                                   List<ChannelSubsetter> channelSubsetterList) {
        constrainingNetworkCodes = new ArrayList<String>();
        if(attrSubsetter == null) {
            // nothing
        } else if(attrSubsetter instanceof NetworkOR) {
            NetworkSubsetter[] kids = ((NetworkOR)attrSubsetter).getNetworkSubsetters();
            for(int i = 0; i < kids.length; i++) {
                if(kids[i] instanceof NetworkCode) {
                    constrainingNetworkCodes.add(((NetworkCode)kids[i]).getCode());
                } else {
                    constrainingNetworkCodes.clear();
                    break;
                }
            }
        } else if(attrSubsetter instanceof NetworkCode) {
            constrainingNetworkCodes.add(((NetworkCode)attrSubsetter).getCode());
        } else {
            // nothing
        }

        constrainingStationCodes = new ArrayList<String>();
        if(stationSubsetter == null) {
            // nothing
        } else if(stationSubsetter instanceof StationOR) {
            List<Subsetter> kids = ((StationOR)stationSubsetter).getSubsetters();
            for (Subsetter subsetter : kids) {
                if(subsetter instanceof StationCode) {
                    constrainingStationCodes.add(((StationCode)subsetter).getCode());
                } else {
                    constrainingStationCodes.clear();
                    break;
                }
            }
        } else if(stationSubsetter instanceof StationCode) {
            constrainingStationCodes.add(((StationCode)stationSubsetter).getCode());
        } else {
            // nothing
        }

        constrainingChannelCodes = new ArrayList<String>();
        constrainingLocationCodes = new ArrayList<String>();
        List<ChannelSubsetter> onlySiteList = new ArrayList<ChannelSubsetter>();
        List<ChannelSubsetter> onlyChanList = new ArrayList<ChannelSubsetter>();
        for (ChannelSubsetter subsetter : channelSubsetterList) {
            if ( subsetter instanceof SiteCode) {
                onlySiteList.add(subsetter);
            } else if ( subsetter instanceof ChannelCode) {
                onlyChanList.add(subsetter);
            } else if ( subsetter instanceof ChannelOR) {
                List<Subsetter> chanOrList = ((ChannelOR)subsetter).getSubsetters();
                if (chanOrList.size() == 0) {
                    // weird, but no effect
                } else {
                    boolean isAllChannelCode = true;
                    boolean isAllSiteCode = true;
                    for (Subsetter subsetter2 : chanOrList) {
                        if (subsetter2 instanceof ChannelCode) {
                            isAllChannelCode = true;
                            isAllSiteCode = false;
                        } else if (subsetter2 instanceof SiteCode) {
                            isAllChannelCode = false;
                            isAllSiteCode = true;
                        } else {
                            isAllChannelCode = false;
                            isAllSiteCode = false;
                            break;
                        }
                    }
                    if (isAllSiteCode) {
                        onlySiteList.add(subsetter);
                    }
                    if (isAllChannelCode) {
                        onlyChanList.add(subsetter);
                    }
                }
            } else {
                onlySiteList.add(subsetter);
                onlyChanList.add(subsetter);
            }
        }
        
        
        if(onlyChanList.size() == 0 || onlyChanList.size() > 1) {
            // nothing
        } else if(onlyChanList.size() == 1 && onlyChanList.get(0) instanceof ChannelOR) {
            List<Subsetter> kids = ((ChannelOR)onlyChanList.get(0)).getSubsetters();
            for (Subsetter subsetter : kids) {
                if(subsetter instanceof ChannelCode) {
                    constrainingChannelCodes.add(((ChannelCode)subsetter).getCode());
                } else {
                    constrainingChannelCodes.clear();
                    break;
                }
            }
        } else if(onlyChanList.size() == 1 && onlyChanList.get(0)  instanceof ChannelCode) {
            constrainingChannelCodes.add(((ChannelCode)onlyChanList.get(0)).getCode());
        } else {
            // nothing
        }
        
        if(onlySiteList.size() == 0 || onlySiteList.size() > 1) {
            // nothing
        } else if(onlySiteList.size() == 1 && onlySiteList.get(0) instanceof ChannelOR) {
            List<Subsetter> kids = ((ChannelOR)onlySiteList.get(0)).getSubsetters();
            for (Subsetter subsetter : kids) {
                if(subsetter instanceof SiteCode) {
                    constrainingLocationCodes.add(((SiteCode)subsetter).getCode());
                } else {
                    constrainingLocationCodes.clear();
                    break;
                }
            }
        } else if(onlySiteList.size() == 1 && onlySiteList.get(0)  instanceof SiteCode) {
            constrainingLocationCodes.add(((SiteCode)onlySiteList.get(0)).getCode());
        } else {
            // nothing
        }
    }
    
    public List<String> getConstrainingNetworkCodes() {
        return constrainingNetworkCodes;
    }
    
    public List<String> getConstrainingStationCodes() {
        return constrainingStationCodes;
    }
    
    public List<String> getConstrainingLocationCodes() {
        return constrainingLocationCodes;
    }
    
    public List<String> getConstrainingChannelCodes() {
        return constrainingChannelCodes;
    }

    List<String> constrainingNetworkCodes = new ArrayList<String>();
    List<String> constrainingStationCodes = new ArrayList<String>();
    List<String> constrainingLocationCodes = new ArrayList<String>();
    List<String> constrainingChannelCodes = new ArrayList<String>();

}
