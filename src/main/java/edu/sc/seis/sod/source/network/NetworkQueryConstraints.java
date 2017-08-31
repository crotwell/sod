package edu.sc.seis.sod.source.network;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import edu.sc.seis.sod.model.common.MicroSecondDate;
import edu.sc.seis.sod.model.common.TimeInterval;
import edu.sc.seis.sod.model.common.TimeRange;
import edu.sc.seis.sod.model.common.UnitImpl;
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
import edu.sc.seis.sod.util.time.ClockUtil;


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
                                   List<ChannelSubsetter> channelSubsetterList,
                                   TimeRange timeRange) {
        if (timeRange != null) {
            this.beginConstraint = timeRange.getBeginTime();
            this.endConstraint = timeRange.getEndTime();
            if (endConstraint.isAfter(ClockUtil.now().minus(ClockUtil.ONE_HOUR))) {
                endConstraint = null;
            }
        }
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
        Iterator<ChannelSubsetter> it = channelSubsetterList.iterator();
        boolean secondMightBeChan = false;
        if (it.hasNext()) {
            ChannelSubsetter first = it.next();
            if (first instanceof ChannelOR) {
                boolean isAllChannelCode = true;
                boolean isAllSiteCode = true;
                List<Subsetter> chanOrList = ((ChannelOR)first).getSubsetters();
                for (Subsetter subsetter2 : chanOrList) {
                    if (subsetter2 instanceof ChannelCode) {
                        isAllSiteCode = false;
                    } else if (subsetter2 instanceof SiteCode) {
                        isAllChannelCode = false;
                    } else {
                        isAllChannelCode = false;
                        isAllSiteCode = false;
                        break;
                    }
                }
                if (isAllSiteCode) {
                    onlySiteList.add(first);
                    secondMightBeChan = true; // might be only chan subsetters in second
                }
                if (isAllChannelCode) {
                    onlyChanList.add(first);
                }
            } else if ( first instanceof SiteCode) {
                onlySiteList.add(first);
                secondMightBeChan = true; // might be only chan subsetters in second
            } else if ( first instanceof ChannelCode) {
                onlyChanList.add(first);
            }
            if (it.hasNext() && secondMightBeChan) {
                ChannelSubsetter second = it.next();
                if (second instanceof ChannelOR) {
                    boolean isAllChannelCode = true;
                    List<Subsetter> chanOrList = ((ChannelOR)second).getSubsetters();
                    for (Subsetter subsetter2 : chanOrList) {
                        if (subsetter2 instanceof ChannelCode) {
//                          so far ok
                        } else {
                            isAllChannelCode = false;
                            break;
                        }
                    }
                    if (isAllChannelCode) {
                        onlyChanList.add(second);
                    }
                } else if ( second instanceof ChannelCode) {
                    onlyChanList.add(second);
                }
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

    public Instant getConstrainingBeginTime() {
        return beginConstraint;
    }
    public Instant getConstrainingEndTime() {
        return endConstraint;
    }

    List<String> constrainingNetworkCodes = new ArrayList<String>();
    List<String> constrainingStationCodes = new ArrayList<String>();
    List<String> constrainingLocationCodes = new ArrayList<String>();
    List<String> constrainingChannelCodes = new ArrayList<String>();
    Instant beginConstraint;
    Instant endConstraint;

}
