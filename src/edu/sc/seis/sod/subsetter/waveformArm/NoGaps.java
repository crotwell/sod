package edu.sc.seis.sod.subsetter.waveFormArm;

import org.apache.log4j.Category;
import org.w3c.dom.Element;

import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.IfNetwork.NetworkAccess;
import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.iris.Fissures.model.MicroSecondDate;
import edu.sc.seis.sod.AvailableDataSubsetter;
import edu.sc.seis.sod.CookieJar;
import edu.sc.seis.sod.SodElement;

/**
 * sample xml
 *<pre>
 * &lt;nogaps/&gt;
 *</pre>
 */

public class NoGaps implements AvailableDataSubsetter, SodElement{
    /**
     * Creates a new <code>NoGaps</code> instance.
     *
     * @param config an <code>Element</code> value
     */
    public NoGaps (Element config){
    
    }
    
    /**
     * Describe <code>accept</code> method here.
     *
     * @param event an <code>EventAccessOperations</code> value
     * @param network a <code>NetworkAccess</code> value
     * @param channel a <code>Channel</code> value
     * @param original a <code>RequestFilter[]</code> value
     * @param available a <code>RequestFilter[]</code> value
     * @param cookies a <code>CookieJar</code> value
     * @return a <code>boolean</code> value
     */
    public boolean accept(EventAccessOperations event,
              NetworkAccess network,
              Channel channel,
              RequestFilter[] original,
              RequestFilter[] available,
              CookieJar cookies) {

    boolean ok = true;
    logger.debug("original length="+original.length+"  available legnth="+available.length);
    for(int counter = 0; counter < original.length; counter++) {
        ok = false;
        MicroSecondDate originalStartDate =
        new MicroSecondDate(original[counter].start_time);
        MicroSecondDate originalEndDate =
        new MicroSecondDate(original[counter].end_time);
        for(int subcounter = 0; subcounter < available.length; subcounter++) {
        MicroSecondDate availableStartDate = new MicroSecondDate(available[subcounter].start_time);
        MicroSecondDate availableEndDate = new MicroSecondDate(available[subcounter].end_time);
        logger.debug(originalStartDate+" "+originalEndDate+" - "+availableStartDate+" "+availableEndDate);

        if(( originalStartDate.after(availableStartDate) || originalStartDate.equals(availableStartDate))
           && (originalEndDate.before(availableEndDate) || originalEndDate.equals(availableEndDate))) {
            ok = true;
            break;
        }
        }
        if (ok == false) {
        logger.debug("NoGaps fail");
        return false;
        } // end of if (ok == false)
    }
    return true;
    }

    static Category logger =
    Category.getInstance(NoGaps.class.getName());
    
}// NoGaps
