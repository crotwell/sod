package edu.sc.seis.sod.subsetter.waveFormArm;

import edu.sc.seis.sod.*;
import edu.sc.seis.TauP.*;

import edu.iris.Fissures.IfEvent.*;
import edu.iris.Fissures.event.*;
import edu.iris.Fissures.IfNetwork.*;
import edu.iris.Fissures.network.*;
import edu.iris.Fissures.model.*;
import edu.iris.Fissures.IfSeismogramDC.*;

import java.util.*;

import org.w3c.dom.*;

/** 
 * sample xml file
 *	&lt;phaseRequest&gt;
 *		&lt;beginPhase&gt;ttp&lt;/beginPhase&gt;
 *       	&lt;beginOffset&gt;
 *			&lt;unit&gt;SECOND&lt;/unit&gt;
 *			&lt;value&gt;-120&lt;/value&gt;
 *		&lt;/beginOffset&gt;
 *		&lt;endPhase&gt;tts&lt;/endPhase&gt;
 *		&lt;endOffset&gt;
 *			&lt;unit&gt;SECOND&lt;/unit&gt;
 *			&lt;value&gt;600&lt;/value&gt;
 *		&lt;/endOffset&gt;
 *	&lt;/phaseRequest&gt;
 */



public class PhaseRequest implements RequestGenerator{
    /**
     * Creates a new <code>PhaseRequest</code> instance.
     *
     * @param config an <code>Element</code> value
     */
    public PhaseRequest (Element config) throws ConfigurationException{

	NodeList childNodes = config.getChildNodes();
	Node node;
	for(int counter = 0; counter < childNodes.getLength(); counter++) {
		node = childNodes.item(counter);
		if(node instanceof Element) {

			SodElement sodElement = (SodElement) SodUtil.load((Element)node,
								"edu.sc.seis.sod.subsetter.waveFormArm");
			if(sodElement instanceof BeginPhase) beginPhase = (BeginPhase)sodElement;
			else if(sodElement instanceof BeginOffset) beginOffset = (BeginOffset)sodElement;
			else if(sodElement instanceof EndPhase) endPhase = (EndPhase)sodElement;
			else if(sodElement instanceof EndOffset) endOffset = (EndOffset)sodElement;
		}
	}

    }
    
    /**
     * Describe <code>generateRequest</code> method here.
     *
     * @param event an <code>EventAccessOperations</code> value
     * @param network a <code>NetworkAccess</code> value
     * @param channel a <code>Channel</code> value
     * @param cookies a <code>CookieJar</code> value
     * @return a <code>RequestFilter[]</code> value
     */
    public RequestFilter[] generateRequest(EventAccessOperations event, 
			  NetworkAccess network, 
			  Channel channel, 
			  CookieJar cookies) throws Exception{
	Origin origin = null;
	double originDepth;
	double arrivalStartTime = -100.0;
	double arrivalEndTime = -100.0;
	origin = event.get_preferred_origin();
	Properties props = Start.getProperties();
	String tauPModel = new String();
	try {
	    tauPModel = props.getProperty("edu.sc.seis.sod.TaupModel");
	    	   
	} catch(Exception e) {
	    
	    tauPModel = "prem";
	}
	
	TauP_Time tauPTime = new TauP_Time(tauPModel);
	tauPTime.clearPhaseNames();
	tauPTime.parsePhaseList(beginPhase.getPhase()+" "+endPhase.getPhase());
	UnitImpl originUnit = (UnitImpl)origin.my_location.depth.the_units;
	originDepth = origin.my_location.depth.convertTo(UnitImpl.KILOMETER).value;
	
	tauPTime.setSourceDepth(originDepth);
	tauPTime.calculate(SphericalCoords.distance(origin.my_location.latitude, 
						    origin.my_location.longitude,
						    channel.my_site.my_station.my_location.latitude,
						    channel.my_site.my_station.my_location.longitude));
			   
	Arrival[] arrivals = tauPTime.getArrivals();
	for(int counter = 0; counter < arrivals.length; counter++) {
	    String arrivalName = arrivals[counter].getName();
	    if(beginPhase.getPhase().startsWith("tt")) {
		if(beginPhase.getPhase().equals("tts") && arrivalName.toUpperCase().startsWith("S")) {
		    if(arrivalStartTime == -100.0) arrivalStartTime = arrivals[counter].getTime();
		} else if(beginPhase.getPhase().equals("ttp") && arrivalName.toUpperCase().startsWith("P")) {
		    if(arrivalStartTime == -100.0) arrivalStartTime = arrivals[counter].getTime();
		} 
	    } else if(beginPhase.getPhase().equals(arrivalName)) {
		if(arrivalStartTime == -100.0) arrivalStartTime = arrivals[counter].getTime();
	    }
	    
	    if(endPhase.getPhase().startsWith("tt")) {
		if(endPhase.getPhase().equals("tts") && arrivalName.toUpperCase().startsWith("S")) {
		    if(arrivalEndTime == -100.0) arrivalEndTime = arrivals[counter].getTime();
		} else if(endPhase.getPhase().equals("ttp") && arrivalName.toUpperCase().startsWith("P")) {
		    if(arrivalEndTime == -100.0) arrivalEndTime = arrivals[counter].getTime();
		} 
	    } else if(endPhase.getPhase().equals(arrivalName)) {
		if(arrivalEndTime == -100.0) arrivalEndTime = arrivals[counter].getTime();
	    }
	    
	    if(arrivalStartTime != -100.0 && arrivalEndTime != -100.0) break; 

	}
	/*System.out.println("originDpeth "+originDepth);
	System.out.println("distance "+SphericalCoords.distance(origin.my_location.latitude, 
		 				    origin.my_location.longitude,
			 			    channel.my_site.my_station.my_location.latitude,
				 			       channel.my_site.my_station.my_location.longitude));
	System.out.println("arrivalStartTime = "+arrivalStartTime);
	System.out.println("arrivalEndTime = "+arrivalEndTime);*/
	edu.iris.Fissures.Time originTime = origin.origin_time;
	MicroSecondDate originDate = new MicroSecondDate(originTime);
	TimeInterval bInterval = new TimeInterval(beginOffset.getValue()+arrivalStartTime, UnitImpl.SECOND);
	TimeInterval eInterval = new TimeInterval(endOffset.getValue()+arrivalEndTime, UnitImpl.SECOND);
	MicroSecondDate bDate = originDate.add(bInterval);
	MicroSecondDate eDate = originDate.add(eInterval);
	RequestFilter[] filters;
        filters = new RequestFilter[1];
        filters[0] = 
            new RequestFilter(channel.get_id(),
                              bDate.getFissuresTime(),
			      eDate.getFissuresTime()
                              );
	
	return filters;

    }
   
   private BeginOffset beginOffset;

   private BeginPhase beginPhase;

   private EndOffset endOffset;

   private EndPhase endPhase;
    
}// PhaseRequest
