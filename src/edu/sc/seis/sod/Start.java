package edu.sc.seis.sod;

import edu.iris.Fissures.*;
import edu.iris.Fissures.IfNetwork.*;
import edu.iris.Fissures.network.*;
import edu.iris.Fissures.IfEvent.*;
import edu.iris.Fissures.event.*;
import edu.iris.Fissures.IfSeismogramDC.*;

/**
 * Start.java
 *
 *
 * Created: Thu Dec 13 16:06:00 2001
 *
 * @author <a href="mailto:">Philip Crotwell</a>
 * @version
 */

public class Start {
    public Start () {
	
    }

    public void init() {

    }

    public void start() {
	NetworkAccessOperations net;
	EventAccessOperations event;
	while ((event = eventQuery.hasNext()) != null) {
	    if ( ! eventCheck.accept(event)) {
		continue;
	    }
	    while ((net = networkQuery.hasNext()) != null) {
		if ( ! networkIdCheck.accept(net.get_id())) {
		    continue;
		}
		if ( ! networkCheck.accept(net)) {
		    continue;
		}
		StationIterator stationCheck = new StationIterator(net);
		if (! stationCheck.accept(station)) {
		    continue;
		}
	
		
	    } // end of while ((net = networkQuery.hasNext()) != null)
	    
	    if (checkNetwork(net)) {
		   
	    } // end of if (checkNetwork(net))
		
	 
	    
	} // end of while (moreevents)
	
    }
    
    public static void main (String[] args) {
	 
    } // end of main ()

    /** Event selection arm. */
    void checkEvent() {
	
    }

    void checkChannel() {

    }

    void nextWaveform() {

    }    

    NetworkDC netDC;
    EventDC eventDC;
    DataCenter seisDC;

}// Start
