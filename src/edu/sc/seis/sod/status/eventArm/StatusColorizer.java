/**
 * StatusColorizer.java
 *
 * @author Charles Groves
 */

package edu.sc.seis.sod.status.eventArm;

import java.awt.Color;
import java.util.Iterator;

import com.bbn.openmap.omGraphics.OMGraphicList;

import edu.sc.seis.fissuresUtil.display.DisplayUtils;
import edu.sc.seis.fissuresUtil.map.colorizer.event.EventColorizer;
import edu.sc.seis.fissuresUtil.map.graphics.OMEvent;
import edu.sc.seis.sod.Stage;
import edu.sc.seis.sod.Standing;
import edu.sc.seis.sod.Status;
import edu.sc.seis.sod.hibernate.StatefulEvent;

public class StatusColorizer implements EventColorizer {
    private static final Color SUCCESS_COLOR = DisplayUtils.STATION;
    private static final Color FAILURE_COLOR = DisplayUtils.DOWN_STATION;
    private static final Status IN_PROG = Status.get(Stage.EVENT_CHANNEL_POPULATION,
                                                     Standing.IN_PROG);
    private static final  Status SUCCESS =  Status.get(Stage.EVENT_CHANNEL_POPULATION,
                                                       Standing.SUCCESS);

    /**
     * Method colorize requires that all OMEvents in this OMGraphicList be
     * instances of StatefulEvent
     */
    public void colorize(OMGraphicList events) {
        Iterator it = events.iterator();
        while(it.hasNext()){
            OMEvent cur = (OMEvent)it.next();
            StatefulEvent curEv = (StatefulEvent)cur.getEvent();
            if(curEv.getStatus().equals(SUCCESS) ||
               curEv.getStatus().equals(IN_PROG)){ cur.setPaint(SUCCESS_COLOR);}
               else{ cur.setPaint(FAILURE_COLOR); }
        }
    }

}

