package edu.sc.seis.sod.subsetter.eventArm;

import edu.sc.seis.sod.*;
import edu.iris.Fissures.IfEvent.*;
import org.w3c.dom.*;

import java.io.*;

/**
 * &lt;printLineEventProcess/&gt;
 */


public class NullEventProcess implements EventArmProcess {
    /**
     * Creates a new <code>NullEventProcess</code> instance.
     *
     * @param config an <code>Element</code> value
     */
    public NullEventProcess (Element config){
	
    }

    /**
     * Describe <code>process</code> method here.
     *
     * @param event an <code>EventAccessOperations</code> value
     * @param cookies a <code>CookieJar</code> value
     */
    public void process(EventAccessOperations event, CookieJar cookies) {
    }
}// NullEventProcess
