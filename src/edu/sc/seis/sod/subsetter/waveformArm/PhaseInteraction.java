package edu.sc.seis.sod.subsetter.waveFormArm;

import edu.sc.seis.sod.*;
import edu.sc.seis.sod.subsetter.*;

import edu.iris.Fissures.IfEvent.*;
import edu.iris.Fissures.event.*;
import edu.iris.Fissures.IfNetwork.*;
import edu.iris.Fissures.network.*;

import edu.iris.Fissures.*;

import org.w3c.dom.*;

/**
 * PhaseInteraction.java
 *
 *
 * Created: Mon Apr  8 16:32:56 2002
 *
 * @author <a href="mailto:">Srinivasa Telukutla</a>
 * @version
 */

public class PhaseInteraction implements EventStationSubsetter {
    /**
     * Creates a new <code>PhaseInteraction</code> instance.
     *
     * @param config an <code>Element</code> value
     */
    public PhaseInteraction (Element config) throws ConfigurationException{

	Element element = SodUtil.getElement(config,"modelName");
	if(element != null) modelName = SodUtil.getNestedText(element);
	element = SodUtil.getElement(config,"phaseName");
	if(element != null) phaseName = SodUtil.getNestedText(element);
	element = SodUtil.getElement(config,"interactionStyle");
	if(element != null) interactionStyle = SodUtil.getNestedText(element);
	element = SodUtil.getElement(config, "interactionNumber");
	if(element != null) interactionNumber = Integer.parseInt(SodUtil.getNestedText(element));
	element = SodUtil.getElement(config, "relative");
	if(element != null) phaseInteractionType = (PhaseInteractionType) SodUtil.load(element, "edu.sc.seis.sod.subsetter.waveFormArm");
	element = SodUtil.getElement(config, "absolute");
	if(element != null) phaseInteractionType = (PhaseInteractionType) SodUtil.load(element, "edu.sc.seis.sod.subsetter.waveFormArm");
	

    }
    
    /**
     * Describe <code>accept</code> method here.
     *
     * @param eventAccess an <code>EventAccessOperations</code> value
     * @param network a <code>NetworkAccess</code> value
     * @param station a <code>Station</code> value
     * @param cookies a <code>CookieJar</code> value
     * @return a <code>boolean</code> value
     */
    public boolean accept(EventAccessOperations eventAccess,  NetworkAccess network,Station station, CookieJar cookies) {
	return true;
    }

    private String modelName = null;
    
    private String phaseName = null;

    private String interactionStyle = null;

    private int interactionNumber = -1;

    private PhaseInteractionType phaseInteractionType = null;
  

}// PhaseInteraction
