package edu.sc.seis.sod.subsetter.eventArm;

import edu.sc.seis.sod.*;
import edu.sc.seis.fissuresUtil.namingService.*;

import java.util.*;
import org.w3c.dom.*;
import edu.iris.Fissures.IfEvent.*;
import edu.iris.Fissures.event.*;
import edu.iris.Fissures.*;
import org.apache.log4j.*;


/**
 * EventChannelFinder.java
 *
 *
 * Created: Fri May 24 14:02:41 2002
 *
 * @author <a href="mailto:">Srinivasa Telukutla</a>
 * @version
 */

public class EventChannelFinder extends AbstractSource implements SodElement, Runnable{
    public EventChannelFinder (Element config) throws Exception{
    super(config);
    this.config = config;
        CommonAccess commonAccess = CommonAccess.getCommonAccess();
    fissuresNamingService = commonAccess.getFissuresNamingService();
    processConfig();
  }

    /**
     * Describe <code>processConfig</code> method here.
     *
     * @exception ConfigurationException if an error occurs
     */
    protected void processConfig() throws Exception{
    eventDC = fissuresNamingService.getEventDC(getDNSName(), getSourceName());
    Element element = SodUtil.getElement(config, "eventchannelname");
    if(element != null) eventChannelName = SodUtil.getNestedText(element);
    }

    public String getEventChannelName() {
    return this.eventChannelName;
    }

    public void run() {
    PushClient pushClient = new PushClient(eventDC, eventChannelName, eventArm);
    }

    public void setEventArm(EventArm eventArm) {

    this.eventArm = eventArm;
    }

    private EventArm eventArm;

    private EventDC eventDC;

    private FissuresNamingService fissuresNamingService = null;

    private Element config;

    private String eventChannelName = null;

    static Category logger =
       Category.getInstance(EventChannelFinder.class.getName());

}// EventChannelFinder
