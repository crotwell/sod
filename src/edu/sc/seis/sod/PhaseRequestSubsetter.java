package edu.sc.seis.sod;

import edu.iris.Fissures.IfEvent.*;

/**
 * PhaseRequestSubsetter.java
 *
 *
 * Created: Tue Apr  2 13:32:13 2002
 *
 * @author <a href="mailto:telukutl@piglet">Srinivasa Telukutla</a>
 * @version
 */

public interface PhaseRequestSubsetter extends Subsetter{
    public boolean accept(CookieJar cookies);
    
}// PhaseRequestSubsetter
