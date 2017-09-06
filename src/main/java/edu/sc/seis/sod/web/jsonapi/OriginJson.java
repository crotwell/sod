package edu.sc.seis.sod.web.jsonapi;

import org.json.JSONException;
import org.json.JSONWriter;

import edu.sc.seis.sod.model.common.Location;
import edu.sc.seis.sod.model.common.QuantityImpl;
import edu.sc.seis.sod.model.common.UnitImpl;
import edu.sc.seis.sod.model.event.OriginImpl;


public class OriginJson extends AbstractJsonApiData {

    public OriginJson(OriginImpl origin, String baseUrl) {
        super(baseUrl);
        this.origin = origin;
    }
    
    @Override
    public String getType() {
        return "origin";
    }

    @Override
    public String getId() {
        return ""+origin.getDbid();
    }
    
    @Override
    public void encodeAttributes(JSONWriter out) throws JSONException {
        Location loc = origin.getLocation();
        out.key("time").value(origin.getTime().toString())
        .key("latitude").value(loc.latitude)
        .key("longitude").value(loc.longitude)
        .key("elevation").value(((QuantityImpl)loc.elevation).getValue(UnitImpl.METER))
        .key("depth").value(((QuantityImpl)loc.depth).getValue(UnitImpl.KILOMETER))
        .key("contributor").value(origin.getContributor())
        .key("catalog").value(origin.getCatalog());
    }

    OriginImpl origin;
}
