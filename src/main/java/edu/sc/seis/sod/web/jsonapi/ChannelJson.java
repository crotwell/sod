package edu.sc.seis.sod.web.jsonapi;

import org.json.JSONException;
import org.json.JSONWriter;

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
        return "channel";
    }

    @Override
    public String getId() {
        return ""+chan.getDbid();
       // StationJson staJson = new StationJson((StationImpl)chan.getSite().getStation(), baseUrl);
       // return staJson.getId()+"."+chan.getSite().get_code()+"."+chan.get_code()+"."+chan.getId().begin_time.date_time;
    }

    @Override
    public void encodeAttributes(JSONWriter out) throws JSONException {
        out.key("site-code")
        .value(chan.getSite().get_code())
        .key("channel-code")
        .value(chan.get_code())
        .key("station")
        .value(chan.getStationImpl().getDbid())
        .key("sps")
        .value(((SamplingImpl)chan.getSamplingInfo()).getFrequency().getValue(UnitImpl.HERTZ))
        .key("latitude")
        .value(chan.getSite().getLocation().latitude)
        .key("longitude")
        .value(chan.getSite().getLocation().longitude)
        .key("elevation")
        .value(((QuantityImpl)chan.getSite().getLocation().elevation).getValue(UnitImpl.METER))
        .key("depth")
        .value(((QuantityImpl)chan.getSite().getLocation().depth).getValue(UnitImpl.METER));
    }

    @Override
    public boolean hasRelationships() {
        return true;
    }

    @Override
    public void encodeRelationships(JSONWriter out) throws JSONException {
        StationJson staJson = new StationJson(chan.getStationImpl(), getBaseUrl());
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
