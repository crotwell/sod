package edu.sc.seis.sod.web.jsonapi;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONWriter;

import edu.sc.seis.seisFile.TimeUtils;
import edu.sc.seis.seisFile.fdsnws.stationxml.Channel;
import edu.sc.seis.seisFile.fdsnws.stationxml.Station;
import edu.sc.seis.sod.model.common.QuantityImpl;
import edu.sc.seis.sod.model.common.UnitImpl;


public class StationJson extends AbstractJsonApiData {

    public StationJson(Station sta, String baseUrl) {
        this(sta, null, baseUrl);
    }
     
    public StationJson(Station sta, List<Channel> chanList, String baseUrl) {
        super(baseUrl);
        this.sta = sta;
        this.chanList = chanList;
    }
    
    @Override
    public String getType() {
        return "station";
    }

    @Override
    public String getId() {
        return new NetworkJson(sta.getNetwork(), baseUrl).getId()+"."+sta.getStationCode();
    }

    @Override
    public void encodeAttributes(JSONWriter out) throws JSONException {
        out.key("station-code").value(sta.getStationCode())
	        .key("name").value(sta.getName())
	        .key("start-time").value(TimeUtils.toISOString(sta.getStartDateTime()))
	        .key("end-time").value(NetworkJson.encodeEndTime(sta.getEndDateTime()))
	    	.key("latitude").value(sta.getLatitude())
	    	.key("longitude").value(sta.getLongitude())
	    	.key("elevation").value(sta.getElevation().getValue());
        if (sta.getSite() != null ) {
            out.key("site").object()
            	.key("name").value(sta.getSite().getName())
            	.key("description").value(sta.getSite().getDescription())
            	.endObject();
        }
    }

    @Override
    public boolean hasRelationships() {
        return true;
    }

    @Override
    public void encodeRelationships(JSONWriter out) throws JSONException {
        out.key("network").object();
        out.key("data").object();
        out.key("id").value(new NetworkJson(sta.getNetwork(), baseUrl).getId());
        out.key("type").value("network");
        out.endObject();// end data
        out.endObject();// net network
        out.key("esps")
                .object()
                .key("links")
                .object()
                .key("related")
                .value(formEventRelationshipURL(sta));
        out.endObject(); // end links
        out.endObject(); // end esps
        out.key("channels").object();
        out.key("links").object();
        out.key("related").value(formChannelRelationshipURL(sta));
        out.endObject(); // end links
        out.endObject(); // end channels
    }
    
    @Override
    public boolean hasLinks() {
        return true;
    }

    @Override
    public void encodeLinks(JSONWriter out) throws JSONException {
        out.key("self").value(formStationURL(sta));
        
    }

    public static List<JsonApiData> toJsonList(List<Station> staList, String baseUrl) {
        List<JsonApiData> out = new ArrayList<JsonApiData>(staList.size());
        for (Station stationImpl : staList) {
            out.add(new StationJson(stationImpl, baseUrl));
        }
        return out;
    }
    
    public String formStationURL(Station sta) {
        NetworkJson netJson = new NetworkJson(sta.getNetworkAttr(), baseUrl);
        String out = baseUrl+"/networks/"+netJson.getId()+"/stations/"+getId();
        return out;
    }

    public String formEventRelationshipURL(Station sta) {
        String out = baseUrl+"/stations/"+getId()+"/quakes";
        return out;
    }
    public String formChannelRelationshipURL(Station sta) {
        String out = baseUrl+"/stations/"+getId()+"/channels";
        return out;
    }
    
    Station sta;
    
    List<Channel> chanList;
}
