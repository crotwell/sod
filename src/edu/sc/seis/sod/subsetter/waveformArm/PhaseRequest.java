package edu.sc.seis.sod.subsetter.waveFormArm;

import edu.sc.seis.sod.*;

import edu.iris.Fissures.IfEvent.*;
import edu.iris.Fissures.event.*;

import edu.iris.Fissures.IfNetwork.*;
import edu.iris.Fissures.network.*;

import edu.iris.Fissures.model.*;

import edu.iris.Fissures.IfSeismogramDC.*;

import org.w3c.dom.*;

/**
 * PhaseRequest.java
 *
 *
 * Created: Mon Apr  8 15:03:55 2002
 *
 * @author <a href="mailto:">Srinivasa Telukutla</a>
 * @version
 */


public class PhaseRequest implements RequestGenerator{
    /**
     * Creates a new <code>PhaseRequest</code> instance.
     *
     * @param config an <code>Element</code> value
     */
    public PhaseRequest (Element config) throws ConfigurationException{

	System.out.println("In the constructor of PhaseRequest");
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
	System.out.println("the begin phase is "+beginPhase.getPhase());
	System.out.println("the end phase is "+endPhase.getPhase());
	System.out.println("the beginOffset unit is "+beginOffset.getUnit());
	System.out.println("the beginOffset value is "+beginOffset.getValue());
	System.out.println("the endOffset unit is "+endOffset.getUnit());
	System.out.println("the endOffset value is "+endOffset.getValue());
	Origin origin = null;
	try {
	    origin = event.get_preferred_origin();
	} catch(NoPreferredOrigin npoe) {
	    
	    System.out.println("caught Exception no Preferred Origin");
	}
	edu.iris.Fissures.Time originTime = origin.origin_time;
	System.out.println("originTime is "+origin.origin_time.date_time);
	MicroSecondDate originDate = new MicroSecondDate(originTime);
	TimeInterval bInterval = new TimeInterval(beginOffset.getValue(), UnitImpl.SECOND);
	TimeInterval eInterval = new TimeInterval(endOffset.getValue(), UnitImpl.SECOND);
	MicroSecondDate bDate = originDate.add(bInterval);
	MicroSecondDate eDate = originDate.add(eInterval);
	RequestFilter[] filters;
        filters = new RequestFilter[1];
        filters[0] = 
            new RequestFilter(channel.get_id(),
                              bDate.getFissuresTime(),
			      eDate.getFissuresTime()
                              );
	
	
	System.out.println("beginTime is !!!!!!!!!!!!!!!!! "+bDate.getFissuresTime().date_time);
	System.out.println("endTime is !!!!!!!!!!!!!!!!!"+eDate.getFissuresTime().date_time);
	return filters;

    }
   
   private BeginOffset beginOffset;

   private BeginPhase beginPhase;

   private EndOffset endOffset;

   private EndPhase endPhase;
    
}// PhaseRequest
