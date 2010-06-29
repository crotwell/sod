package edu.sc.seis.sod.subsetter.eventStation;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Element;

import edu.iris.Fissures.IfEvent.Origin;
import edu.iris.Fissures.model.QuantityImpl;
import edu.iris.Fissures.model.UnitImpl;
import edu.iris.Fissures.network.StationImpl;
import edu.sc.seis.TauP.Arrival;
import edu.sc.seis.TauP.SeismicPhase;
import edu.sc.seis.TauP.TauModelException;
import edu.sc.seis.TauP.TauP_SetSac;
import edu.sc.seis.fissuresUtil.bag.DistAz;
import edu.sc.seis.fissuresUtil.cache.CacheEvent;
import edu.sc.seis.fissuresUtil.display.configuration.DOMHelper;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.CookieJar;
import edu.sc.seis.sod.SodUtil;
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
        beginOffset = SodUtil.loadTimeInterval(DOMHelper.extractElement(config, "endOffset")).getValue(UnitImpl.SECOND);
        endOffset = SodUtil.loadTimeInterval(DOMHelper.extractElement(config, "endOffset")).getValue(UnitImpl.SECOND);
        for (Element element : phElements) {
            phases.add(new SeismicPhase(SodUtil.getNestedText(element),
                                        modelName,
                                        0.0));
        }
        arrivalIndex = DOMHelper.extractInt(config, "arrivalIndex", 1);
        // shift to zero based index, so positive index minus one, neg index stays same
        if (arrivalIndex > 0) { arrivalIndex -= 1; }
    }
    
    @Override
    public StringTree accept(CacheEvent event, StationImpl station, CookieJar cookieJar) throws Exception {

        Origin origin = event.get_preferred_origin();
        double depth = ((QuantityImpl)origin.getLocation().depth).getValue(UnitImpl.KILOMETER);
        List<List<Arrival>> arrivals = calcArrivals(depth, new DistAz(station.getLocation(),
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
        double windowEnd = mainTime + beginOffset;
        List<List<Arrival>> interfereArrivals = arrivals.subList(1, arrivals.size());
        for (List<Arrival> list : interfereArrivals) {
            if (onlyFirst) {
                if (list.size() != 0) {
                    if (list.get(0).getTime() >= windowBegin && list.get(0).getTime() >= windowEnd ) {
                        return new Fail(this, list.get(0).getName()+" "+list.get(0).getTime());
                    }
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
            List<SeismicPhase> newPhases = new ArrayList<SeismicPhase>();
            for (SeismicPhase phase : phases) {
                newPhases.add(new SeismicPhase(phase.getName(),
                                              modelName,
                                              depth));
            }
            phases = newPhases;
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
    List<SeismicPhase> phases; // main phase is index 0, interferring phases are 1 to end
    List<String> interferingPhaseNames;
}
