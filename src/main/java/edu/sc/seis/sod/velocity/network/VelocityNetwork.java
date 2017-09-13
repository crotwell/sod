package edu.sc.seis.sod.velocity.network;

import java.time.Instant;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.velocity.VelocityContext;

import edu.sc.seis.seisFile.fdsnws.stationxml.Network;
import edu.sc.seis.sod.model.station.NetworkId;
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
    }
    
    public Network getWrapped() {
        return net;
    }

    public NetworkId getId() {
        return net.getId();
    }

    public String getCode() {
        return net.getCode();
    }

    public String getCodeWithYear() {
        return net.toString();
    }
    
    public String getName() {
        return getDescription();
    }
    
    public String getDescription() {
        return FissuresFormatter.oneLineAndClean(super.getDescription());
    }
    
    public String getRawDescription() {
        return super.getDescription();
    }

    public String getStart() {
        return net.getStartDate();
    }
    
    public String getStart(String format){
        return SimpleVelocitizer.format(net.getStartDateTime(), format);
    }
    
    public Instant getStartDateTime() {
        return net.getStartDateTime();
    }

    public String getEnd() {
        return net.getEndDate();
    }
    
    public String getEnd(String format){
        return SimpleVelocitizer.format(net.getEndDateTime(), format);
    }

    public Instant getEndDateTime() {
        return net.getEndDateTime();
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
            return sta0.getCode().compareTo(sta1.getCode());
        }
    };
}