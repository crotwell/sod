package edu.sc.seis.sod.velocity.network;

import java.time.Instant;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.velocity.VelocityContext;

import edu.sc.seis.seisFile.fdsnws.stationxml.Network;
import edu.sc.seis.sod.model.station.NetworkId;
import edu.sc.seis.sod.model.station.NetworkIdUtil;
import edu.sc.seis.sod.status.FissuresFormatter;
import edu.sc.seis.sod.velocity.SimpleVelocitizer;

/**
 * @author groves Created on Jan 7, 2005
 */
public class VelocityNetwork extends Network {

    /**
     * Creates a VelocityNetwork with no stations. Will throw
     * UnsupportedOperationException if getStations is called
     */
    public VelocityNetwork(Network net) {
        this(net, null);
    }

    public VelocityNetwork(List<VelocityStation> stations) {
        this((Network)getFirstStation(stations).getWrapped().getNetworkAttr(),
             stations);
    }
    
    private static final VelocityStation getFirstStation(List<VelocityStation> stations) {
        if (stations.size() == 0) {
            throw new IllegalArgumentException("station list cannot be of size 0");
        }
        return stations.get(0);
    }

    public VelocityNetwork(Network net, List<VelocityStation> stations) {
        this.net = net;
        this.stations = stations;
        if (stations != null) {
            Collections.sort(this.stations, byCodeSorter);
        }
        this.setName(net.getName());
        this.setDescription(net.getDescription());
        this.setOwner(net.getOwner());
        this.setEffectiveTime(net.getEffectiveTime());
    }
    
    public Network getWrapped() {
        return net;
    }

    public NetworkId get_id() {
        return net.get_id();
    }

    public String get_code() {
        return net.get_code();
    }

    public String getCode() {
        return get_code();
    }

    public String getCodeWithYear() {
        return NetworkIdUtil.toStringNoDates(net.get_id());
    }
    
    public String getName() {
        return FissuresFormatter.oneLineAndClean(super.getName());
    }
    
    public String getRawName() {
        return super.getName();
    }
    
    public String getRawBeginDate() {
        return net.get_id().begin_time.getISOTime();
    }

    public Instant getStart() {
        return getEffectiveTime().getBeginTime();
    }
    
    public String getStart(String format){
        return SimpleVelocitizer.format(getStart(), format);
    }

    /**
     * @deprecated - use getStart instead
     */
    public Instant getStartDate() {
        return getStart();
    }

    public Instant getEnd() {
        return getEffectiveTime().getEndTime();
    }
    
    public String getEnd(String format){
        return SimpleVelocitizer.format(getEnd(), format);
    }

    /**
     * @deprecated use getEnd instead
     */
    public Instant getEndDate() {
        return getEnd();
    }

    /**
     * just like getName except special characters for xml are made into entity
     * references. This just replaces the main ones: amp, lt, gt, apos, quot.
     */
    public String getEntityRefName() {
        String s = getName();
        s = s.replaceAll("&", "&amp;");
        s = s.replaceAll("<", "&lt;");
        s = s.replaceAll(">", "&gt;");
        s = s.replaceAll("'", "&apos;");
        s = s.replaceAll("\"", "&quote;");
        return s;
    }

    public List<VelocityStation> getStations() {
        if(stations == null) {
            throw new UnsupportedOperationException("Stations have not been added for this network!");
        }
        return stations;
    }
    
    public int getDbid() {
        return getWrapped().getDbid();
    }
    
    public int getDbId() {
        return getDbid();
    }
    
    public String toString(){
        return getCodeWithYear();
    }

    private List<VelocityStation> stations;

    private Network net;

    public void insertIntoContext(VelocityContext ctx) {
        ctx.put("network", this);
        ctx.put("net", this);
    }

    public static VelocityNetwork wrap(Network net) {
        if(net instanceof VelocityNetwork) {
            return (VelocityNetwork)net;
        }
        return new VelocityNetwork((Network)net);
    }
    
    Comparator<VelocityStation> byCodeSorter = new Comparator<VelocityStation>() {
        @Override
        public int compare(VelocityStation sta0, VelocityStation sta1) {
            return sta0.get_code().compareTo(sta1.get_code());
        }
    };
}