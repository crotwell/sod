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
 * BeginOffset.java
 *
 *
 * Created: Mon Apr  8 16:41:31 2002
 *
 * @author <a href="mailto:">Srinivasa Telukutla</a>
 * @version
 */

public class BeginOffset extends Interval implements PhaseRequestSubsetter{
    public BeginOffset (Element config){
	super(config);
    }

    public boolean accept(CookieJar cookies) {

	return true;

    }
    
}// BeginOffset
