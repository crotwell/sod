package edu.sc.seis.sod.velocity.network;

import java.util.List;
import org.apache.velocity.VelocityContext;
import edu.iris.Fissures.IfNetwork.NetworkAttr;
import edu.iris.Fissures.IfNetwork.NetworkId;
import edu.iris.Fissures.model.MicroSecondDate;
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
    public VelocityNetwork(NetworkAttr net) {
        this(net, -1);
    }

    /**
     * Creates a VelocityNetwork with no stations. Will throw
     * UnsupportedOperationException if getStations is called
     */
    public VelocityNetwork(NetworkAttr net, int dbid) {
        this(net, dbid, null);
    }

    public VelocityNetwork(List stations) {
        this(((VelocityStation)stations.get(0)).my_network, stations);
    }

    public VelocityNetwork(NetworkAttr net, List stations) {
        this(net, -1, stations);
    }

    private VelocityNetwork(NetworkAttr net, int dbid, List stations) {
        this.net = net;
        this.stations = stations;
        this.name = net.name;
        this.description = net.description;
        this.owner = net.owner;
        this.effective_time = net.effective_time;
        this.dbid = dbid;
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
        return new MicroSecondDate(effective_time.start_time);
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
        return new MicroSecondDate(effective_time.end_time);
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

    private NetworkAttr net;

    public void insertIntoContext(VelocityContext ctx) {
        ctx.put("network", this);
    }
}