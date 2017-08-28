package edu.sc.seis.sod.subsetter.eventStation;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;

import edu.sc.seis.TauP.Arrival;
import edu.sc.seis.TauP.SeismicPhase;
import edu.sc.seis.TauP.TauModelException;
import edu.sc.seis.TauP.TauP_Time;
import edu.sc.seis.seisFile.fdsnws.stationxml.Station;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.DOMHelper;
import edu.sc.seis.sod.SodUtil;
import edu.sc.seis.sod.hibernate.eventpair.CookieJar;
import edu.sc.seis.sod.model.common.DistAz;
import edu.sc.seis.sod.model.common.Location;
import edu.sc.seis.sod.model.common.QuantityImpl;
import edu.sc.seis.sod.model.common.UnitImpl;
import edu.sc.seis.sod.model.event.CacheEvent;
import edu.sc.seis.sod.model.event.OriginImpl;
import edu.sc.seis.sod.status.Fail;
import edu.sc.seis.sod.status.Pass;
import edu.sc.seis.sod.status.StringTree;


public class PhaseWithoutInterference extends PhaseExists implements EventStationSubsetter {

    public PhaseWithoutInterference(Element config) throws ConfigurationException, TauModelException {
        super(config);
        List<Element> phElements = SodUtil.getAllElements(config, "interferingPhase");
        mainPhase = new SeismicPhase(phaseName,
                                     modelName,
                                     0.0);
        beginOffset = SodUtil.loadTimeInterval(DOMHelper.extractElement(config, "beginOffset")).getValue(UnitImpl.SECOND);
        endOffset = SodUtil.loadTimeInterval(DOMHelper.extractElement(config, "endOffset")).getValue(UnitImpl.SECOND);
        for (Element element : phElements) {
            List<String> newPhases = TauP_Time.getPhaseNames(SodUtil.getNestedText(element));
            for (String s : newPhases) {
                if ( ! s.equals(phaseName)) {
                    interferingPhaseNames.add(s);
                } else {
                    logger.warn("Phase cannot interfer with itself: "+phaseName+", skipping...");
                }
            }
        }
        arrivalIndex = DOMHelper.extractInt(config, "arrivalIndex", 1);
        // shift to zero based index, so positive index minus one, neg index stays same
        if (arrivalIndex > 0) { arrivalIndex -= 1; }
    }
    
    @Override
    public StringTree accept(CacheEvent event, Station station, CookieJar cookieJar) throws Exception {

        OriginImpl origin = event.get_preferred_origin();
        double depth = ((QuantityImpl)origin.getLocation().depth).getValue(UnitImpl.KILOMETER);
        List<List<Arrival>> arrivals = calcArrivals(depth, new DistAz(Location.of(station),
                                                                      origin.getLocation()).getDelta());
        List<Arrival> mainArrivals = arrivals.get(0);
        double mainTime;
        if ( arrivalIndex >= 0 && mainArrivals.size() > arrivalIndex) {
            mainTime = mainArrivals.get(arrivalIndex).getTime();
        } else if ( arrivalIndex < 0 && mainArrivals.size() > -1*arrivalIndex) {
            mainTime = mainArrivals.get(mainArrivals.size()+arrivalIndex).getTime();
        } else {
            return new Fail(this, "No arrival for "+phaseName+" ("+(arrivalIndex<0?arrivalIndex:arrivalIndex+1)+")");
        }
        
        double windowBegin = mainTime + beginOffset;
        double windowEnd = mainTime + endOffset;
        List<List<Arrival>> interfereArrivals = arrivals.subList(1, arrivals.size());
        for (List<Arrival> list : interfereArrivals) {
            for (Arrival arrival : list) {
                logger.debug("check: "+arrival.getName()+"  "+windowBegin+" < "+arrival.getTime()+" < "+windowEnd);
                if (arrival.getTime() >= windowBegin && arrival.getTime() <= windowEnd ) {
                    return new Fail(this, arrival.getName()+" ("+arrival.getTime()+" s)   interferes with "+ mainPhase.getName()+" ("+mainTime+" s)");
                }
                if (onlyFirst) {
                    // only do first arrival in the list
                    break;
                }
            }
        }
        return new Pass(this);
    }
    
    /** calcs arrivals. 
     * @throws TauModelException */
    protected synchronized List<List<Arrival>> calcArrivals(double depth, double degrees) throws TauModelException {
        if (mainPhase.getTauModel().getSourceDepth() != depth) {
            mainPhase = new SeismicPhase(phaseName,
                                         modelName,
                                         depth);
            phases.clear();
            for (String phaseName : interferingPhaseNames) {
                phases.add(new SeismicPhase(phaseName,
                                              modelName,
                                              depth));
            }
        }
        List<List<Arrival>> out = new ArrayList<List<Arrival>>();
        out.add(mainPhase.calcTime(degrees));
        for (SeismicPhase phase : phases) {
            out.add(phase.calcTime(degrees));
        }
        return out;
    }
    
    boolean onlyFirst = true;
    double beginOffset, endOffset;
    
    SeismicPhase mainPhase;
    int arrivalIndex = 0;
    List<SeismicPhase> phases = new ArrayList<SeismicPhase>(); // main phase is index 0, interferring phases are 1 to end
    List<String> interferingPhaseNames = new ArrayList<String>();
    
    private static Logger logger = LoggerFactory.getLogger(PhaseWithoutInterference.class);
}
