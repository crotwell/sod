package edu.sc.seis.sod.hibernate;

import java.util.List;

import edu.sc.seis.seisFile.fdsnws.stationxml.Channel;
import edu.sc.seis.seisFile.fdsnws.stationxml.Network;
import edu.sc.seis.seisFile.fdsnws.stationxml.Station;
import edu.sc.seis.sod.model.common.TimeRange;
import edu.sc.seis.sod.model.station.ChannelIdUtil;
import edu.sc.seis.sod.model.station.StationIdUtil;


public class NetworkConsistencyCheck {

    public static boolean isConsistent(Network net, Station sta) {
        TimeRange staRange = new TimeRange(sta);
        TimeRange netRange = new TimeRange(net);
        if (netRange.getBeginTime().isAfter(staRange.getBeginTime())) {
            logger.warn("Network begins after station: "+net.toString()+"  "+StationIdUtil.toString(sta));
            return false;
        }
        return true;
    }

    public static boolean isConsistent(List<? extends Station> staList) {
        if (staList.size() < 2) {
            return true;
        }
        List<? extends Station> subStaList = staList.subList(1, staList.size());
        Station first = staList.get(0);
        for (Station nextSta : subStaList) {
            if ( ! isConsistent(first, nextSta)) {
                return false;
            }
        }
        return isConsistent(subStaList);
    }
    
    public static boolean isConsistent(Station staA, Station staB) {
        if ( ! staA.getNetwork().getCode().equals(staB.getNetwork().getCode()) 
                || ! staA.getCode().equals(staB.getCode())) {
            //different stations, so ok
            return true;
        }
        TimeRange staARange = new TimeRange(staA);
        TimeRange staBRange = new TimeRange(staB);
        if (staARange.intersects(staBRange)) {
            logger.warn("Station overlaps other station: "+StationIdUtil.toString(staA)+"-"+staA.getEndDate()
                         +"   "+StationIdUtil.toString(staB)+"-"+staB.getEndDate().toString());
            return false;
        }
        return true;
    }

    public static boolean isConsistent(Station sta, Channel chan) {
        TimeRange staRange = new TimeRange(sta);
        TimeRange chanRange = new TimeRange(chan);
        if (staRange.getBeginTime().isAfter(chanRange.getBeginTime())) {
            logger.warn("Station begins after channel: "+ChannelIdUtil.toStringNoDates(chan)+" "+chanRange
                         +"    "+StationIdUtil.toString(sta)+" "+staRange);
            return false;
        }
        return true;
    }   

    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(NetworkConsistencyCheck.class);
}