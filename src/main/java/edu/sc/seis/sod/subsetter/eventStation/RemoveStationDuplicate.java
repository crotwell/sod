package edu.sc.seis.sod.subsetter.eventStation;

import java.util.List;

import org.w3c.dom.Element;

import edu.sc.seis.fissuresUtil.xml.XMLUtil;
import edu.sc.seis.seisFile.fdsnws.stationxml.Station;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.SodUtil;
import edu.sc.seis.sod.hibernate.SodDB;
import edu.sc.seis.sod.hibernate.eventpair.CookieJar;
import edu.sc.seis.sod.model.common.DistAz;
import edu.sc.seis.sod.model.common.Location;
import edu.sc.seis.sod.model.common.QuantityImpl;
import edu.sc.seis.sod.model.common.UnitImpl;
import edu.sc.seis.sod.model.event.CacheEvent;
import edu.sc.seis.sod.status.Fail;
import edu.sc.seis.sod.status.Pass;
import edu.sc.seis.sod.status.StringTree;


public class RemoveStationDuplicate implements EventStationSubsetter {

    
    public StringTree accept(CacheEvent event, Station station, CookieJar cookieJar) throws Exception {
        List<Station> passStations = SodDB.getSingleton().getSuccessfulStationsForEvent(event);
        for (Station stationImpl : passStations) {
            if (isDistanceClose(station, stationImpl)) {
                return new Fail(this);
            }
        }
        return new Pass(this);
    }

    public boolean isDistanceClose(Station staA, Station staB) {
        DistAz distAz = new DistAz(Location.of(staA), Location.of(staB));
        if (maxDistance.getUnit().isConvertableTo(UnitImpl.DEGREE)) {
        return distAz.getDelta() < maxDistance.convertTo(UnitImpl.DEGREE).getValue();
        } else {
            // use earth radius of 6371 km
            return distAz.getDelta()*6371 < maxDistance.convertTo(UnitImpl.KILOMETER).getValue();
        }
    }
    
    public RemoveStationDuplicate(Element config) throws ConfigurationException {
        Element el = XMLUtil.getElement(config, "maxDistance");
        if (el != null){
            setMaxDistance(SodUtil.loadQuantity(el));
            
        }
    }
    
    public RemoveStationDuplicate(QuantityImpl maxDistance) throws ConfigurationException {
        setMaxDistance(maxDistance);
    }
    
    public RemoveStationDuplicate() {
        
    }
    
    protected void setMaxDistance(QuantityImpl maxDistance) throws ConfigurationException {
        if ( ! ( maxDistance.getUnit().isConvertableTo(UnitImpl.DEGREE) || maxDistance.getUnit().isConvertableTo(UnitImpl.KILOMETER))) {
            throw new ConfigurationException("Units must be convertible to DEGREE or KILOMETER: "+maxDistance.getUnit());
        }
        this.maxDistance = maxDistance;
    }
    
    protected QuantityImpl maxDistance = new QuantityImpl(0.5, UnitImpl.DEGREE);
    
}
