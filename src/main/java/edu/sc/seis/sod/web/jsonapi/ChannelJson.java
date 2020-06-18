package edu.sc.seis.sod.web.jsonapi;

import org.json.JSONException;
import org.json.JSONWriter;

import edu.sc.seis.seisFile.TimeUtils;
import edu.sc.seis.seisFile.fdsnws.stationxml.Channel;
import edu.sc.seis.sod.model.common.QuantityImpl;
import edu.sc.seis.sod.model.common.SamplingImpl;
import edu.sc.seis.sod.model.common.UnitImpl;


public class ChannelJson extends AbstractJsonApiData {

    public ChannelJson(Channel chan, String baseUrl) {
        super(baseUrl);
        this.chan = chan;
    }
    
    @Override
    public String getType() {
        return "channels";
    }

    @Override
    public String getId() {
        return ""+chan.getDbid();
       // StationJson staJson = new StationJson((StationImpl)chan.getSite().getStation(), baseUrl);
       // return staJson.getId()+"."+chan.getSite().get_code()+"."+chan.get_code()+"."+chan.getId().begin_time.date_time;
    }

    @Override
    public void encodeAttributes(JSONWriter out) throws JSONException {
        out.key("loc-code")
        .value(chan.getLocCode())
        .key("channel-code")
        .value(chan.getChannelCode())
        .key("station")
        .value(chan.getStation().getDbid())
        .key("sps")
        .value(chan.getSampleRate().getValue())
        .key("latitude")
        .value(chan.getLatitude().getValue())
        .key("longitude")
        .value(chan.getLongitude().getValue())
        .key("elevation")
        .value(chan.getElevation().getValue())
        .key("depth")
        .value(chan.getDepth().getValue())
        .key("start-time").value(TimeUtils.toISOString(chan.getStartDateTime()))
        .key("end-time").value(NetworkJson.encodeEndTime(chan.getEndDateTime()));
    }

    @Override
    public boolean hasRelationships() {
        return true;
    }

    @Override
    public void encodeRelationships(JSONWriter out) throws JSONException {
        StationJson staJson = new StationJson(chan.getStation(), getBaseUrl());
        out.key("station").object();
        out.key("id").value(staJson.getId());
        out.key("type").value(staJson.getType());
        out.endObject();
    }
    
    @Override
    public boolean hasLinks() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void encodeLinks(JSONWriter out) throws JSONException {
        // TODO Auto-generated method stub
        
    }
    
    Channel chan;
}
