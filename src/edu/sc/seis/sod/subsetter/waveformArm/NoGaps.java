package edu.sc.seis.sod.subsetter.waveformArm;

import org.apache.log4j.Logger;
import org.w3c.dom.Element;

import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.IfNetwork.NetworkAccess;
import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.iris.Fissures.model.MicroSecondDate;
import edu.sc.seis.sod.subsetter.waveformArm.AvailableDataSubsetter;
import edu.sc.seis.sod.SodElement;

/**
 * sample xml
 *<pre>
 * &lt;nogaps/&gt;
 *</pre>
 */

public class NoGaps implements AvailableDataSubsetter, SodElement{
    public NoGaps (Element config){}

    public boolean accept(EventAccessOperations event, Channel channel,
                          RequestFilter[] original, RequestFilter[] available) {

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

    private static Logger logger =
        Logger.getLogger(NoGaps.class);

}// NoGaps
