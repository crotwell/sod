package edu.sc.seis.sod.velocity.network;

import java.io.StringWriter;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.apache.velocity.VelocityContext;

import edu.iris.Fissures.IfNetwork.Station;
import edu.iris.Fissures.IfNetwork.StationId;
import edu.iris.Fissures.model.MicroSecondDate;
import edu.iris.Fissures.model.QuantityImpl;
import edu.iris.Fissures.model.UnitImpl;
import edu.iris.Fissures.network.NetworkAttrImpl;
import edu.iris.Fissures.network.StationIdUtil;
import edu.iris.Fissures.network.StationImpl;
import edu.sc.seis.fissuresUtil.bag.DistAz;
import edu.sc.seis.fissuresUtil.xml.XMLStation;
import edu.sc.seis.fissuresUtil.xml.XMLUtil;
import edu.sc.seis.sod.status.FissuresFormatter;
import edu.sc.seis.sod.velocity.SimpleVelocitizer;
import edu.sc.seis.sod.velocity.event.VelocityEvent;

/**
 * @author groves Created on Jan 7, 2005
 */
public class VelocityStation extends StationImpl {

    public VelocityStation(StationImpl sta) {
        if (sta == null) {
            throw new IllegalArgumentException("StationImpl cannot be null");
        }
        this.sta = sta;
        name = sta.getName();
        setLocation(sta.getLocation());
        setEffectiveTime(sta.getEffectiveTime());
        operator = sta.getOperator();
        description = sta.getDescription();
        comment = sta.getComment();
        setNetworkAttr(sta.getNetworkAttr());
    }

    public int getDbId() {
        return sta.getDbid();
    }

    public StationId get_id() {
        return sta.get_id();
    }
    
    public StationId getId() {
        return sta.getId();
    }

    public String get_code() {
        return sta.get_code();
    }

    public String getCode() {
        return get_code();
    }

    public String getCodes() {
        return getNetCode() + "." + getCode();
    }

    public String getNetCode() {
        return getNet().get_code();
    }

    public VelocityNetwork getNet() {
        if(velocityNet == null) {
            velocityNet = new VelocityNetwork((NetworkAttrImpl)getNetworkAttr());
        }
        return velocityNet;
    }

    public MicroSecondDate getStartDate() {
        return new MicroSecondDate(getEffectiveTime().start_time);
    }

    public MicroSecondDate getEndDate() {
        return new MicroSecondDate(getEffectiveTime().end_time);
    }

    public String getStart() {
        return FissuresFormatter.formatDate(getEffectiveTime().start_time);
    }

    public String getStart(String dateFormat) {
        if(dateFormat.equals("longfile")) {
            return FissuresFormatter.formatDateForFile(getEffectiveTime().start_time);
        }
        return SimpleVelocitizer.format(new MicroSecondDate(getEffectiveTime().start_time),
                                        dateFormat);
    }

    public String getEnd() {
        return FissuresFormatter.formatDate(getEffectiveTime().end_time);
    }

    public String getEnd(String dateFormat) {
        if(dateFormat.equals("longfile")) {
            return FissuresFormatter.formatDateForFile(getEffectiveTime().end_time);
        }
        return SimpleVelocitizer.format(new MicroSecondDate(getEffectiveTime().end_time),
                                        dateFormat);
    }

    public String getName() {
        return name;
    }

    public String getCSVName() {
        return name.replaceAll(",", "");
    }

    public String getDescription() {
        return description;
    }

    public String getOperator() {
        return operator;
    }

    public String getComment() {
        return comment;
    }

    public String getLatitude() {
        return df.format(sta.getLocation().latitude);
    }

    public String getLatitude(String format) {
        return new DecimalFormat(format).format(sta.getLocation().latitude);
    }

    public String getLongitude() {
        return df.format(sta.getLocation().longitude);
    }

    public String getLongitude(String format) {
        return new DecimalFormat(format).format(sta.getLocation().longitude);
    }


    public String getOrientedLatitude() {
        return getOrientedLatitude(sta.getLocation().latitude);
    }
    
    public static String getOrientedLatitude(float latitude) {
        if(latitude < 0) {
            return df.format(-latitude) + " S";
        }
        return df.format(latitude) + " N";
    }

    public String getOrientedLongitude() {
        return getOrientedLongitude(sta.getLocation().longitude);
    }

    public static String getOrientedLongitude(float longitude) {
        if(longitude < 0) {
            return df.format(-longitude) + " W";
        }
        return df.format(longitude) + " E";
    }

    public Float getFloatLatitude() {
        return new Float(getLocation().latitude);
    }

    public Float getFloatLongitude() {
        return new Float(getLocation().longitude);
    }
    
    public String getDepth() {
        return FissuresFormatter.formatDepth(QuantityImpl.createQuantityImpl(sta.getLocation().depth));
    }

    public String getDepth(String format) {
        double depthInKM = QuantityImpl.createQuantityImpl(sta.getLocation().depth)
                .convertTo(UnitImpl.KILOMETER).value;
        return new DecimalFormat(format).format(depthInKM);
    }

    public String getElevation() {
        return FissuresFormatter.formatDepth(QuantityImpl.createQuantityImpl(sta.getLocation().elevation));
    }

    public String getElevation(String format) {
        double elevInMeters = QuantityImpl.createQuantityImpl(sta.getLocation().elevation)
                .convertTo(UnitImpl.METER).value;
        return new DecimalFormat(format).format(elevInMeters);
    }

    public String getDistance(VelocityEvent event) {
        double km = DistAz.degreesToKilometers(new DistAz(this, event).getDelta());
        return FissuresFormatter.formatDistance(new QuantityImpl(km,
                                                                 UnitImpl.KILOMETER));
    }

    public String getDistanceDeg(VelocityEvent event) {
        return FissuresFormatter.formatDistance(getDist(event));
    }

    public String getAz(VelocityEvent event) {
        double az = new DistAz(this, event).getAz();
        return FissuresFormatter.formatQuantity(new QuantityImpl(az,
                                                                 UnitImpl.DEGREE));
    }

    public QuantityImpl getDist(VelocityEvent event) {
        double deg = new DistAz(this, event).getDelta();
        return new QuantityImpl(deg, UnitImpl.DEGREE);
    }

    public String getBaz(VelocityEvent event) {
        double baz = new DistAz(this, event).getBaz();
        return FissuresFormatter.formatQuantity(new QuantityImpl(baz,
                                                                 UnitImpl.DEGREE));
    }

    public String getURL() {
        return "stations/" + getNetCode() + "/" + getCode();
    }

    public String toXML() throws XMLStreamException {
        StringWriter writer = new StringWriter();
        XMLStreamWriter xmlWriter = XMLUtil.staxOutputFactory.createXMLStreamWriter(writer);
        XMLStation.insert(xmlWriter, this);
        return writer.toString();
    }

    public String toString() {
        return StationIdUtil.toString(get_id());
    }

    public boolean equals(Object o) {
        if(o == this) {
            return true;
        }
        if(o instanceof VelocityStation) {
            VelocityStation oVel = (VelocityStation)o;
            if(oVel.getDbId() != -1 && getDbId() != -1 && oVel.getDbId() == getDbId()) {
                return true;
            }
        }
        if(o instanceof Station) {
            Station oSta = (Station)o;
            return StationIdUtil.areEqual(oSta, sta);
        }
        return false;
    }

    public int hashCode() {
        if(!hashCalc) {
            hash = StationIdUtil.toString(get_id()).hashCode();
            hashCalc = true;
        }
        return hash;
    }

    private boolean hashCalc = false;

    private int hash = 0;

    private VelocityNetwork velocityNet = null;

    private StationImpl sta;
    
    private int[] position;

    public void setPosition(int[] position) {
        this.position = position;
    }

    public int[] getPosition() {
        return position;
    }

    static final DecimalFormat df = new DecimalFormat("0.00");

    public void insertIntoContext(VelocityContext ctx) {
        ctx.put("station", this);
        ctx.put("sta", this);
        getNet().insertIntoContext(ctx);
    }
    
    public StationImpl getWrapped() {
        return sta;
    }

    public static VelocityStation[] wrap(List stations) {
        VelocityStation[] out = new VelocityStation[stations.size()];
        for(int i = 0; i < out.length; i++) {
            out[i] = new VelocityStation((StationImpl)stations.get(i));
        }
        return out;
    }

    public static VelocityStation[] wrap(Station[] stations) {
        VelocityStation[] out = new VelocityStation[stations.length];
        for(int i = 0; i < out.length; i++) {
            out[i] = new VelocityStation((StationImpl)stations[i]);
        }
        return out;
    }
    
    public static List<VelocityStation> wrapList(List<? extends Station> stations) {
        List<VelocityStation> out = new ArrayList<VelocityStation>();
        for(Station s : stations) {
            if (s instanceof VelocityStation) {
                out.add((VelocityStation)s);
            } else {
                out.add(new VelocityStation((StationImpl)s));
            }
        }
        return out;
    }
    
}