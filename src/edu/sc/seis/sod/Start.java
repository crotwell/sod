package edu.sc.seis.sod;

import edu.iris.Fissuers.*;
import edu.iris.Fissuers.IfNetwork.*;
import edu.iris.Fissuers.IfEvent.*;
import edu.iris.Fissuers.IfSeismogramDC.*;

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
		if ( ! networkIdCheck.accept(net.get)id()) {
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
		    if (
		} // end of if (checkNetwork(net))
		
	    } // end of if (checkEvent(event))
	    
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
