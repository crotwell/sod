package edu.sc.seis.sod.velocity.network;

import java.util.List;
import org.apache.velocity.VelocityContext;
import edu.iris.Fissures.IfNetwork.NetworkAttr;
import edu.iris.Fissures.IfNetwork.NetworkId;
import edu.iris.Fissures.model.MicroSecondDate;
import edu.iris.Fissures.network.NetworkIdUtil;

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

    private VelocityNetwork(NetworkAttr net, List stations) {
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

    public MicroSecondDate getStartDate() {
        return new MicroSecondDate(effective_time.start_time);
    }

    public MicroSecondDate getEndDate() {
        return new MicroSecondDate(effective_time.end_time);
    }

    public String getName() {
        return name;
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

    private int dbid = -1;

    private List stations;

    private NetworkAttr net;

    public void insertIntoContext(VelocityContext ctx) {
        ctx.put("network", this);
    }
}