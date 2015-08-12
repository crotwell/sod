package edu.sc.seis.sod.web.jsonapi;

import org.json.JSONException;
import org.json.JSONWriter;

import edu.iris.Fissures.model.QuantityImpl;
import edu.iris.Fissures.model.SamplingImpl;
import edu.iris.Fissures.model.UnitImpl;
import edu.iris.Fissures.network.ChannelImpl;


public class ChannelJson extends AbstractJsonApiData {

    public ChannelJson(ChannelImpl chan, String baseUrl) {
        super(baseUrl);
        this.chan = chan;
    }
    
    @Override
    public String getType() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getId() {
        return chan.getSite().get_code()+"."+chan.get_code()+"."+chan.getId().begin_time.date_time;
    }

    @Override
    public void encodeAttributes(JSONWriter out) throws JSONException {
        out.key("siteCode")
        .value(chan.getSite().get_code())
        .key("channelCode")
        .value(chan.get_code())
        .key("station")
        .value(chan.getStationImpl().getDbid())
        .key("sps")
        .value(((SamplingImpl)chan.getSamplingInfo()).getFrequency())
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
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void encodeRelationships(JSONWriter out) throws JSONException {
        // TODO Auto-generated method stub
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
    
    ChannelImpl chan;
}
