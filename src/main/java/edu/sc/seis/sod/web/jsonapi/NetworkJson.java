package edu.sc.seis.sod.web.jsonapi;

import org.json.JSONException;
import org.json.JSONWriter;

import edu.iris.Fissures.Time;
import edu.iris.Fissures.IfNetwork.NetworkAttr;
import edu.iris.Fissures.model.ISOTime;
import edu.iris.Fissures.model.MicroSecondDate;
import edu.iris.Fissures.network.NetworkIdUtil;
import edu.sc.seis.fissuresUtil.chooser.ClockUtil;

public class NetworkJson extends AbstractJsonApiData {

    public NetworkJson(NetworkAttr net, String baseUrl) {
        super(baseUrl);
        this.net = net;
    }

    @Override
    public String getType() {
        return "network";
    }

    @Override
    public String getId() {
        String s = net.get_code();
        if (NetworkIdUtil.isTemporary(net.getId())) {
            s += "_" + net.getBeginTime().date_time.substring(0, 4); // append
                                                                     // start
                                                                     // year
        }
        return s;
    }

    @Override
    public void encodeAttributes(JSONWriter out) throws JSONException {
        out.key("network-code")
                .value(net.getId().network_code)
                .key("start-time")
                .value(net.getId().begin_time.date_time)
                .key("end-time")
                .value(encodeEndTime(net.getEndTime()))
                .key("description")
                .value(net.getDescription());
    }

    @Override
    public boolean hasRelationships() {
        return true;
    }

    @Override
    public void encodeRelationships(JSONWriter out) throws JSONException {
        out.key("stations")
                .object()
                .key("links")
                .object()
                .key("self")
                .value(formStationRelationshipURL(net))
                .key("related")
                .value(formStationListURL(net));
        out.endObject(); // end links
        out.endObject(); // end stations
    }

    @Override
    public boolean hasLinks() {
        return true;
    }

    @Override
    public void encodeLinks(JSONWriter out) throws JSONException {
        out.key("self").value(formNetworkURL(net));
    }

    public String formStationRelationshipURL(NetworkAttr net) {
        String out = baseUrl + "/networks/" + getId() + "/relationships/stations";
        return out;
    }

    public String formNetworkURL(NetworkAttr net) {
        String out = baseUrl + "/networks/" + getId();
        return out;
    }

    public String formStationListURL(NetworkAttr net) {
        String out = baseUrl + "/networks/" + getId() + "/stations";
        return out;
    }

    public static Object encodeEndTime(Time endTime) {
        MicroSecondDate endDate = new MicroSecondDate(endTime);
        if (endDate.before(ClockUtil.now())) {
            return ISOTime.getISOString(endDate);
        } else {
            return null;
        }
    }

    NetworkAttr net;
}
