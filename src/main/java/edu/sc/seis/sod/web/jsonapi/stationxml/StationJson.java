package edu.sc.seis.sod.web.jsonapi.stationxml;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONWriter;

import edu.sc.seis.seisFile.fdsnws.stationxml.Channel;
import edu.sc.seis.seisFile.fdsnws.stationxml.Equipment;
import edu.sc.seis.seisFile.fdsnws.stationxml.ExternalReference;
import edu.sc.seis.seisFile.fdsnws.stationxml.Operator;
import edu.sc.seis.seisFile.fdsnws.stationxml.Site;
import edu.sc.seis.seisFile.fdsnws.stationxml.Station;
import edu.sc.seis.seisFile.fdsnws.stationxml.StationXMLTagNames;
import edu.sc.seis.sod.web.jsonapi.AbstractJsonApiData;
import edu.sc.seis.sod.web.jsonapi.JsonApi;
import edu.sc.seis.sod.web.jsonapi.JsonApiData;
import edu.sc.seis.sod.web.jsonapi.JsonApiResource;


public class StationJson extends AbstractJsonApiData {


    public static final String TYPE = StationXMLTagNames.STATION;
    

    public static Station decode(JsonApiResource res) {
        Station sta = new Station();
        NetworkJson.decodeBaseNodeAttributes(res, sta);
        sta.setLatitude(res.getAttributeDouble(StationXMLTagNames.LAT).floatValue());
        sta.setLongitude(res.getAttributeDouble(StationXMLTagNames.LON).floatValue());
        sta.setElevation(res.getAttributeDouble(StationXMLTagNames.ELEVATION).floatValue());
        Object o = res.getAttribute(StationXMLTagNames.SITE);
        if (o != null && o instanceof JSONObject)  {
            JSONObject jo = (JSONObject)o;
            Site site = new Site(jo.getString(StationXMLTagNames.NAME),
                                 jo.getString(StationXMLTagNames.DESCRIPTION),
                                 jo.getString(StationXMLTagNames.TOWN),
                                 jo.getString(StationXMLTagNames.COUNTY),
                                 jo.getString(StationXMLTagNames.REGION),
                                 jo.getString(StationXMLTagNames.COUNTRY));
            sta.setSite(site);
        }
        sta.setWaterlevel(res.getAttributeDouble(StationXMLTagNames.WATERLEVEL).floatValue());
        sta.setVault(res.getAttributeString(StationXMLTagNames.VAULT));
        sta.setGeology(res.getAttributeString(StationXMLTagNames.GEOLOGY));
        for (Object e : res.getAttributeArray(StationXMLTagNames.EQUIPMENT)) {
            if (e instanceof JSONObject) {
                sta.appendEquipment(EquipmentJson.decode((JSONObject)e));
            }
        }
        for (Object e : res.getAttributeArray(StationXMLTagNames.OPERATOR)) {
            if (e instanceof JSONObject) {
                sta.appendOperator(OperatorJson.decode((JSONObject)e));
            }
        }
        for (Object e : res.getAttributeArray(StationXMLTagNames.EXTERNALREFERENCE)) {
            if (e instanceof JSONObject) {
                sta.appendExternalReference(ExternalReferenceJson.decode((JSONObject)e));
            }
        }
        return sta;
    }
    
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
        return TYPE;
    }

    @Override
    public String getId() {
        return new NetworkJson(sta.getNetwork(), baseUrl).getId()+"_"+sta.getStationCode();
    }

    @Override
    public void encodeAttributes(JSONWriter out) throws JSONException {

        NetworkJson.encodeBaseNodeAttributes(out, sta);
        out.key(StationXMLTagNames.LAT).value(sta.getLatitude())
	    	.key(StationXMLTagNames.LON).value(sta.getLongitude())
	    	.key(StationXMLTagNames.ELEVATION).value(sta.getElevation().getValue());
        if (sta.getSite() != null ) {
            out.key(StationXMLTagNames.SITE).object();
            JsonApi.doKeyValue(out, StationXMLTagNames.NAME, sta.getSite().getName());
            JsonApi.doKeyValue(out, StationXMLTagNames.DESCRIPTION, sta.getSite().getDescription());
            out.endObject();
        }
        JsonApi.doKeyValue(out, StationXMLTagNames.WATERLEVEL, sta.getWaterlevel().getValue());
        JsonApi.doKeyValue(out, StationXMLTagNames.VALUE, sta.getVault());
        JsonApi.doKeyValue(out, StationXMLTagNames.GEOLOGY, sta.getGeology());
        if (sta.getEquipmentList().size() != 0) {
            out.key(StationXMLTagNames.EQUIPMENT).array();
            for (Equipment c : sta.getEquipmentList()) {
                out.object();
                new EquipmentJson(c).encodeAttributes(out);
                out.endObject();
            }
            out.endArray();
        }

        if (sta.getOperatorList().size() != 0) {
            out.key(StationXMLTagNames.OPERATOR).array();
            for (Operator c : sta.getOperatorList()) {
                out.object();
                new OperatorJson(c).encodeAttributes(out);
                out.endObject();
            }
            out.endArray();
        }
        if (sta.getExternalReferenceList().size() != 0) {
            out.key(StationXMLTagNames.EXTERNALREFERENCE).array();
            for (ExternalReference c : sta.getExternalReferenceList()) {
                out.object();
                new ExternalReferenceJson(c).encodeAttributes(out);
                out.endObject();
            }
            out.endArray();
        }
    }

    @Override
    public boolean hasRelationships() {
        return true;
    }

    @Override
    public void encodeRelationships(JSONWriter out) throws JSONException {
        out.key(StationXMLTagNames.NETWORK).object();
        out.key("data").object();
        out.key("id").value(new NetworkJson(sta.getNetwork(), baseUrl).getId());
        out.key("type").value(NetworkJson.TYPE);
        out.endObject();// end data
        out.endObject();// net network
        out.key("quake-station-pairs")
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
        if (chanList.size() != 0) {
            out.key(JsonApi.DATA).array();
            for (Channel c : chanList) {
                out.object();
                ChannelJson cj = new ChannelJson(c, baseUrl);
                out.key(JsonApi.TYPE).value(cj.getType());
                out.key(JsonApi.ID).value(cj.getId());
                out.endObject();
            }
            out.endArray();
        }
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
        NetworkJson netJson = new NetworkJson(sta.getNetwork(), baseUrl);
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
