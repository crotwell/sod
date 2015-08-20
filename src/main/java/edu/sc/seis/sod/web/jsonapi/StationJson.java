package edu.sc.seis.sod.web.jsonapi;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONWriter;

import edu.iris.Fissures.model.QuantityImpl;
import edu.iris.Fissures.model.UnitImpl;
import edu.iris.Fissures.network.StationImpl;


public class StationJson extends AbstractJsonApiData {

    public StationJson(StationImpl sta, String baseUrl) {
        super(baseUrl);
        this.sta = sta;
    }
    
    @Override
    public String getType() {
        return "station";
    }

    @Override
    public String getId() {
        return new NetworkJson(sta.getNetworkAttrImpl(), baseUrl).getId()+"."+sta.get_code();
    }

    @Override
    public void encodeAttributes(JSONWriter out) throws JSONException {
        out.key("station-code").value(sta.get_code())
        .key("name").value(sta.getName())
        .key("start-time").value(sta.getBeginTime().date_time)
        .key("end-time").value(NetworkJson.encodeEndTime(sta.getEndTime()))
        .key("description").value(sta.getDescription())
              .key("latitude").value(sta.getLocation().latitude)
              .key("longitude").value(sta.getLocation().longitude)
              .key("elevation").value(((QuantityImpl)sta.getLocation().elevation).getValue(UnitImpl.METER));
    }

    @Override
    public boolean hasRelationships() {
        return true;
    }

    @Override
    public void encodeRelationships(JSONWriter out) throws JSONException {
        out.key("network").object();
        out.key("data").object();
        out.key("id").value(new NetworkJson(sta.getNetworkAttrImpl(), baseUrl).getId());
        out.key("type").value("network");
        out.endObject();
        out.endObject();
        out.key("esps")
                .object()
                .key("links")
                .object()
                .key("related")
                .value(formEventRelationshipURL(sta));
        out.endObject(); // end links
        out.endObject(); // end events
    }
    
    @Override
    public boolean hasLinks() {
        return true;
    }

    @Override
    public void encodeLinks(JSONWriter out) throws JSONException {
        out.key("self").value(formStationURL(sta));
        
    }

    public static List<JsonApiData> toJsonList(List<StationImpl> staList, String baseUrl) {
        List<JsonApiData> out = new ArrayList<JsonApiData>(staList.size());
        for (StationImpl stationImpl : staList) {
            out.add(new StationJson(stationImpl, baseUrl));
        }
        return out;
    }
    
    public String formStationURL(StationImpl sta) {
        NetworkJson netJson = new NetworkJson(sta.getNetworkAttr(), baseUrl);
        String out = baseUrl+"/networks/"+netJson.getId()+"/stations/"+getId();
        return out;
    }
    
    public String formEventRelationshipURL(StationImpl sta) {
        String out = baseUrl+"/stations/"+getId()+"/events";
        return out;
        
    }
    
    StationImpl sta;
}
