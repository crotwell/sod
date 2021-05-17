package edu.sc.seis.sod.web.jsonapi.stationxml;

import java.sql.ShardingKey;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONWriter;

import edu.sc.seis.seisFile.TimeUtils;
import edu.sc.seis.seisFile.fdsnws.stationxml.BaseNodeType;
import edu.sc.seis.seisFile.fdsnws.stationxml.Comment;
import edu.sc.seis.seisFile.fdsnws.stationxml.Identifier;
import edu.sc.seis.seisFile.fdsnws.stationxml.Network;
import edu.sc.seis.seisFile.fdsnws.stationxml.Person;
import edu.sc.seis.seisFile.fdsnws.stationxml.Station;
import edu.sc.seis.seisFile.fdsnws.stationxml.StationIterator;
import edu.sc.seis.seisFile.fdsnws.stationxml.StationXMLTagNames;
import edu.sc.seis.sod.model.station.NetworkIdUtil;
import edu.sc.seis.sod.util.time.ClockUtil;
import edu.sc.seis.sod.web.jsonapi.AbstractJsonApiData;
import edu.sc.seis.sod.web.jsonapi.JsonApi;
import edu.sc.seis.sod.web.jsonapi.JsonApiResource;

public class NetworkJson extends AbstractJsonApiData {
    
    public static final String CODE = "code";
    public static final String STARTTIME = "starttime";
    public static final String ENDTIME = "endtime";
    public static final String DESCRIPTION = "description";
    public static final String TYPE = "networks";

    public static Network decode(JsonApiResource res) {
        Network net = new Network();
        decodeBaseNodeAttributes(res, net);
        return net;
    }

    
    public NetworkJson(Network net, String baseUrl) {
        this(net, new ArrayList<Station>(), baseUrl);
    }
    
    public NetworkJson(Network net, List<Station> stationList, String baseUrl) {
        super(baseUrl);
        this.net = net;
        this.stationList = stationList;
    }

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public String getId() {
        String s = net.getCode();
        if (NetworkIdUtil.isTemporary(net)) {
            s += net.getStartYearString(); // append start year
        }
        return s;
    }

    @Override
    public void encodeAttributes(JSONWriter out) throws JSONException {
        encodeBaseNodeAttributes(out, net);
    }
    
    public static void encodeBaseNodeAttributes(JSONWriter out, BaseNodeType b) {
        out.key(CODE)
                .value(b.getCode())
                .key(STARTTIME)
                .value(TimeUtils.toISOString(b.getStartDateTime()))
                .key(ENDTIME)
                .value(encodeEndTime(b.getEndDateTime()))
                .key(DESCRIPTION)
                .value(b.getDescription());
        out.key(StationXMLTagNames.IDENTIFIER).array();
        for (Identifier  ident : b.getIdentifierList()) {
            out.object();
            out.key(StationXMLTagNames.TYPE).value(ident.getType());
            out.key(StationXMLTagNames.VALUE).value(ident.getValue());
            out.endObject();
        }
        out.endArray();
        out.key(StationXMLTagNames.COMMENT).array();
        for (Comment comment : b.getCommentList()) {
            out.object();
            new CommentJson(comment).encodeAttributes(out);
            out.endObject();
        }
        out.endArray();
    }
    

    public static BaseNodeType decodeBaseNodeAttributes(JsonApiResource res, BaseNodeType b) {
        b.setCode(res.getAttributeString(CODE));
        b.setStartDate(res.getAttributeString(STARTTIME));
        b.setEndDate(res.getAttributeString(ENDTIME));
        b.setDescription(res.getAttributeString(DESCRIPTION));
        for (Object o : res.getAttributeArray(StationXMLTagNames.IDENTIFIER)) {
            if (o instanceof JSONObject) {
                JSONObject jo = (JSONObject)o;
                Identifier i = new Identifier(jo.getString(StationXMLTagNames.VALUE), jo.getString(StationXMLTagNames.TYPE));
            }
        }
        for (Object o : res.getAttributeArray(StationXMLTagNames.COMMENT)) {
            if (o instanceof JSONObject) {
                b.addComment(CommentJson.decode((JSONObject)o));
            }
        }
        return b;
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
        if (stationList.size() != 0) {
            out.key(JsonApi.DATA).array();
            for (Station s : stationList) {
                out.object();
                StationJson sj = new StationJson(s, baseUrl);
                out.key(JsonApi.ID).value(sj.getId());
                out.key(JsonApi.TYPE).value(sj.getType());
                out.endObject();
            }
            out.endArray();
        }
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

    public String formStationRelationshipURL(Network net) {
        String out = baseUrl + "/networks/" + getId() + "/relationships/stations";
        return out;
    }

    public String formNetworkURL(Network net) {
        String out = baseUrl + "/networks/" + getId();
        return out;
    }

    public String formStationListURL(Network net) {
        String out = baseUrl + "/networks/" + getId() + "/stations";
        return out;
    }

    public static Object encodeEndTime(Instant endDate) {
        if (endDate != null && endDate.isBefore(ClockUtil.now())) {
            return TimeUtils.toISOString(endDate);
        } else {
            return null;
        }
    }

    Network net;
    
    List<Station> stationList;
}
