package edu.sc.seis.sod.velocity.network;

import java.util.List;
import org.apache.velocity.VelocityContext;
import edu.iris.Fissures.IfNetwork.NetworkAttr;
import edu.iris.Fissures.IfNetwork.NetworkId;
import edu.iris.Fissures.model.MicroSecondDate;
import edu.iris.Fissures.network.NetworkAttrImpl;
import edu.iris.Fissures.network.NetworkIdUtil;
import edu.sc.seis.sod.velocity.SimpleVelocitizer;

/**
 * @author groves Created on Jan 7, 2005
 */
public class VelocityNetwork extends NetworkAttr {

    /**
     * Creates a VelocityNetwork with no stations. Will throw
     * UnsupportedOperationException if getStations is called
     */
    public VelocityNetwork(NetworkAttrImpl net) {
        this(net, -1);
    }

    /**
     * Creates a VelocityNetwork with no stations. Will throw
     * UnsupportedOperationException if getStations is called
     */
    public VelocityNetwork(NetworkAttrImpl net, int dbid) {
        this(net, dbid, null);
    }

    public VelocityNetwork(List<VelocityStation> stations) {
        this((NetworkAttrImpl)getFirstStation(stations).getWrapped().getNetworkAttr(),
             stations);
    }
    
    private static final VelocityStation getFirstStation(List<VelocityStation> stations) {
        if (stations.size() == 0) {
            throw new IllegalArgumentException("station list cannot be of size 0");
        }
        return stations.get(0);
    }

    public VelocityNetwork(NetworkAttrImpl net, List stations) {
        this(net, -1, stations);
    }

    private VelocityNetwork(NetworkAttrImpl net, int dbid, List stations) {
        this.net = net;
        this.stations = stations;
        this.setName(net.getName());
        this.setDescription(net.getDescription());
        this.setOwner(net.getOwner());
        this.setEffectiveTime(net.getEffectiveTime());
        this.dbid = dbid;
    }
    
    public NetworkAttrImpl getWrapped() {
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

    public String getRawBeginDate() {
        return net.get_id().begin_time.date_time;
    }

    public String getRawBeginLeapSeconds() {
        return "" + net.get_id().begin_time.leap_seconds_version;
    }

    public MicroSecondDate getStart() {
        return new MicroSecondDate(getEffectiveTime().start_time);
    }
    
    public String getStart(String format){
        return SimpleVelocitizer.format(getStart(), format);
    }

    /**
     * @deprecated - use getStart instead
     */
    public MicroSecondDate getStartDate() {
        return getStart();
    }

    public MicroSecondDate getEnd() {
        return new MicroSecondDate(getEffectiveTime().end_time);
    }
    
    public String getEnd(String format){
        return SimpleVelocitizer.format(getEnd(), format);
    }

    /**
     * @deprecated use getEnd instead
     */
    public MicroSecondDate getEndDate() {
        return getEnd();
    }

    public String getName() {
        return name;
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

    public String getOwner() {
        return owner;
    }

    public String getDescription() {
        return description;
    }

    public List getStations() {
        if(stations == null) {
            throw new UnsupportedOperationException("Stations have not been added for this network!");
        }
        return stations;
    }

    public void setDbId(int dbid) {
        this.dbid = dbid;
    }

    public int getDbId() {
        return dbid;
    }
    
    public String toString(){
        return NetworkIdUtil.toString(get_id());
    }

    private int dbid = -1;

    private List stations;

    private NetworkAttrImpl net;

    public void insertIntoContext(VelocityContext ctx) {
        ctx.put("network", this);
        ctx.put("net", this);
    }

    public static VelocityNetwork wrap(NetworkAttr net) {
        if(net instanceof VelocityNetwork) {
            return (VelocityNetwork)net;
        }
        return new VelocityNetwork((NetworkAttrImpl)net);
    }
}