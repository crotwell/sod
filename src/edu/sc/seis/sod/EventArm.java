package edu.sc.seis.sod;

import edu.sc.seis.sod.database.*;
import edu.sc.seis.sod.subsetter.eventArm.*;
import edu.sc.seis.fissuresUtil.namingService.*;
import edu.sc.seis.fissuresUtil.cache.CacheEvent;

import edu.iris.Fissures.IfEvent.*;
import edu.iris.Fissures.event.*;
import edu.iris.Fissures.model.*;
import edu.iris.Fissures.*;

import java.util.*;

import org.w3c.dom.*;
import org.apache.log4j.*;

/**
 * This class handles the subsetting of the Events based on the subsetters specified
 * in the configuration file (xml file).
 *
 *
 * Created: Thu Mar 14 14:09:52 2002
 *
 * @author <a href="mailto:">Philip Crotwell</a>
 * @version
 */

public class EventArm extends SodExceptionSource implements Runnable{
    /**
     * Creates a new <code>EventArm</code> instance.
     *
     * @param config an <code>Element</code> value
     * @exception ConfigurationException if an error occurs
     */
    public EventArm (Element config, SodExceptionListener sodExceptionListener, Properties props) throws ConfigurationException {
    if ( ! config.getTagName().equals("eventArm")) {
        throw new IllegalArgumentException("Configuration element must be a EventArm tag");
    }
    this.config = config;
    this.props = props;
    addSodExceptionListener(sodExceptionListener);
    processConfig(config);
    }

    /**
     * The run method of the eventArm.
     *
     */
    public void run() {
    try
    {
        processEventArm();
    } catch(Exception e) {

        logger.error("Exception caught while processing the EventArm", e);
        //notify Exception listeners in case of an exception
        notifyListeners(this, e);
    }
    logger.debug("Before setting the source Alive to be false");
    //  getThreadGroup().list();
    // if eventChannelFinder is false ... i.e., if no eventChannel is configured
    // then the eventArm can signal end of processing
    // by setting the sourceAlive attribute of the EventQueue to be false.

    if(eventChannelFinder == null) Start.getEventQueue().setSourceAlive(false);
    logger.debug("After setting the source Alive to be false");
    //getThreadGroup().list();
    logger.debug("IN EVENT ARM **** The number of events in the eventQueue are "
               +Start.getEventQueue().getLength());

    }

    /**
     * This method processes the eventArm subsetters
     *
     * @param config an <code>Element</code> value
     * @exception ConfigurationException if an error occurs
     */
    protected void processConfig(Element config)
    throws ConfigurationException {

    Start.getEventQueue().setSourceAlive(true);
    NodeList children = config.getChildNodes();
    Node node;
    for (int i=0; i<children.getLength(); i++) {
        node = children.item(i);
        if (node instanceof Element) {
        if (((Element)node).getTagName().equals("description")) {
            // skip description element
            continue;
        }
        Object sodElement = SodUtil.load((Element)node, "edu.sc.seis.sod.subsetter.eventArm");
        if(sodElement instanceof edu.sc.seis.sod.subsetter.eventArm.EventFinder) eventFinderSubsetter = (edu.sc.seis.sod.subsetter.eventArm.EventFinder)sodElement;
        else if(sodElement instanceof edu.sc.seis.sod.subsetter.eventArm.EventChannelFinder) eventChannelFinder = (edu.sc.seis.sod.subsetter.eventArm.EventChannelFinder)sodElement;
        else if(sodElement instanceof EventAttrSubsetter) {
            eventAttrSubsetter = (EventAttrSubsetter) sodElement;

        } else if(sodElement instanceof OriginSubsetter) originSubsetter = (OriginSubsetter)sodElement;
        else if(sodElement instanceof EventArmProcess) eventArmProcess = (EventArmProcess)sodElement;
        } // end of if (node instanceof Element)
    } // end of for (int i=0; i<children.getSize(); i++)

    /*Start.getEventQueue().pop();
    Start.getEventQueue().pop();
    Start.getEventQueue().pop();
    logger.debug("event QueueLength is "+Start.getEventQueue().getLength());*/
    }




    /**
     * Describe <code>processEventArm</code> method here.
     *
     * @exception Exception if an error occurs
     */
    public void processEventArm() throws Exception{

    if(eventChannelFinder != null) {
        eventChannelFinder.setEventArm(this);
        Thread thread = new Thread(eventChannelFinder);
        thread.setName("EventChannelFinder");
        thread.start();
    } else {

        logger.debug("EventChannelFinder is NULL");
    }

    if(eventFinderSubsetter == null) return;
    logger.debug("Event Finder query is being used");
    EventDC eventdc = eventFinderSubsetter.getEventDC();
    finder = eventdc.a_finder();
    String[] searchTypes;



    EventSeqIterHolder eventSeqIterHolder = new EventSeqIterHolder();

    Quantity minDepth;
    Quantity maxDepth;
    float minMagnitude;
    float maxMagnitude;
    if(eventFinderSubsetter.getDepthRange() != null) {

        minDepth = eventFinderSubsetter.getDepthRange().getMinDepth();
        maxDepth = eventFinderSubsetter.getDepthRange().getMaxDepth();

    } else {

        minDepth = new QuantityImpl(-90000.0, UnitImpl.KILOMETER);
        maxDepth = new QuantityImpl(90000.0, UnitImpl.KILOMETER);
    }

    if(eventFinderSubsetter.getMagnitudeRange() == null) {

        minMagnitude = -99.0f;
        maxMagnitude = 99.0f;
        searchTypes = new String[1];
        searchTypes[0] = "%";

    } else {

        minMagnitude = eventFinderSubsetter.getMagnitudeRange().getMinMagnitude().value;
        maxMagnitude = eventFinderSubsetter.getMagnitudeRange().getMaxMagnitude().value;
        searchTypes = eventFinderSubsetter.getMagnitudeRange().getSearchTypes();
    }

    logger.debug("getting events from "+eventFinderSubsetter.getEventTimeRange().getTimeRange().start_time.date_time+" to "+eventFinderSubsetter.getEventTimeRange().getTimeRange().end_time.date_time);
    String magTypesLog = "";
    for (int i=0; i<searchTypes.length; i++) {
        magTypesLog += searchTypes[i]+" ";
    } // end of for (int i=0; i<searchTypes.length; i++)
    logger.debug("mag "+minMagnitude+" to "+maxMagnitude+" for "+magTypesLog);

    String[] catalogs = eventFinderSubsetter.getCatalogs();
    String[] contributors = eventFinderSubsetter.getContributors();

    String catalogLog = "";
    for(int counter = 0;  counter < catalogs.length; counter++) {
        catalogLog += catalogs[counter]+" ";
    }
    logger.debug("catalogs = "+catalogLog);

    String contributorLog = "";
    for(int counter = 0; counter < contributors.length; counter++) {
        contributorLog += contributors[counter]+" ";
    }
    logger.debug("contributors = "+contributorLog);

    logger.debug("At the start of processing event arm");
    //getThreadGroup().list();

    edu.iris.Fissures.Time startTime = Start.getEventQueue().getTime(eventFinderSubsetter.getSourceName(),
                                     eventFinderSubsetter.getDNSName());
    logger.debug("At the start of the process eventArm after instantiaing eventConfigDb");
    //getThreadGroup().list();

//  if(startTime == null) {
//      logger.debug("time is not stored in the database so store it");
//      startTime = eventFinderSubsetter.getEventTimeRange().getStartTime();
//      eventConfigDb.setTime(startTime);
//  }

    if(startTime == null) {
        startTime = eventFinderSubsetter.getEventTimeRange().getStartTime();
        Start.getEventQueue().setTime(eventFinderSubsetter.getSourceName(),
                                      eventFinderSubsetter.getDNSName(),
                                      startTime);
    }
    edu.iris.Fissures.Time endTime = calculateEndTime(startTime,
                              eventFinderSubsetter.getEventTimeRange().getEndTime());

    while(!isFinished(endTime, eventFinderSubsetter.getEventTimeRange().getEndTime())) {

        edu.iris.Fissures.TimeRange timeRange = new edu.iris.Fissures.TimeRange(startTime, endTime);

        EventAccess[] eventAccessOrig =
        finder.query_events(eventFinderSubsetter.getArea(),
                    minDepth,
                    maxDepth,
                    timeRange,
                    searchTypes,
                    minMagnitude,
                    maxMagnitude,
                    eventFinderSubsetter.getCatalogs(),
                    eventFinderSubsetter.getContributors(),
                    10,
                    eventSeqIterHolder
                    );

        logger.debug("Found "+eventAccessOrig.length+" events between "+
                     new MicroSecondDate(startTime)+
                     " and "+new MicroSecondDate(endTime));

        //Thread.sleep(10000);
        EventAccessOperations[] eventAccess =
        new EventAccessOperations[eventAccessOrig.length];
        for(int counter = 0; counter < eventAccess.length; counter++) {

        eventAccess[counter] = new CacheEvent(eventAccessOrig[counter]);
        EventAttr attr = eventAccess[counter].get_attributes();
        handleEventAttrSubsetter(eventAccess[counter], attr);

        }
        //here delete all the events in the queue from the
        //previous day that are successful or failed
        //Start.getEventQueue().delete(Status.COMPLETE_SUCCESS);
        //Start.getEventQueue().delete(Status.COMPLETE_REJECT);
        Start.getEventQueue().incrementTime(eventFinderSubsetter.getSourceName(),
                            eventFinderSubsetter.getDNSName(),
                            getIncrementValue());

        startTime = Start.getEventQueue().getTime(eventFinderSubsetter.getSourceName(),
                              eventFinderSubsetter.getDNSName());

        endTime = calculateEndTime(startTime,
                       eventFinderSubsetter.getEventTimeRange().getEndTime());
        Start.getEventQueue().waitForProcessing();

    }// end of while loop where checking for isFinished.
    //  Start.getEventQueue().setSourceAlive(false);
    logger.debug("Finished processing the event arm.");
    //getThreadGroup().list();
    //eventConfigDb.close();
    //System.exit(0);

    }

    /**
     * handles the EventAttrSubsetter.
     *
     * @param eventAccess an <code>EventAccessOperations</code> value
     * @param eventAttr an <code>EventAttr</code> value
     * @exception Exception if an error occurs
     */
    public void handleEventAttrSubsetter(EventAccessOperations eventAccess, EventAttr eventAttr) throws Exception {

    if(eventAttrSubsetter == null || eventAttrSubsetter.accept(eventAttr, null)) {
        try {
        handleOriginSubsetter(eventAccess, eventAccess.get_preferred_origin());
        } catch(Exception e) {return;}
    }
    }

    /**
     * handles the OriginSubsetter
     *
     * @param eventAccess an <code>EventAccessOperations</code> value
     * @param origin an <code>Origin</code> value
     * @exception Exception if an error occurs
     */
    public void handleOriginSubsetter(EventAccessOperations eventAccess, Origin origin) throws Exception{


    if(originSubsetter.accept(eventAccess, origin, null)) {

        handleEventArmProcess(eventAccess, origin);
    }

    }

    /**
     * handles the eventArmProcess.
     *
     * @param eventAccess an <code>EventAccessOperations</code> value
     * @param origin an <code>Origin</code> value
     * @exception Exception if an error occurs
     */
    public void handleEventArmProcess(EventAccessOperations eventAccess, Origin origin) throws Exception{
    Start.getEventQueue().push(eventFinderSubsetter.getDNSName(),
                   eventFinderSubsetter.getSourceName(),
                   (EventAccess)((CacheEvent)eventAccess).getEventAccess(),
                   origin);
    eventArmProcess.process(eventAccess, null);

    }

    /***
     * returns an endTime based on the increment value specified inthe property file.
     *
     */


    private edu.iris.Fissures.Time calculateEndTime(edu.iris.Fissures.Time startTime,
                            edu.iris.Fissures.Time givenEndTime) {

    MicroSecondDate microSecondDate = new MicroSecondDate(startTime);
    Calendar calendar = Calendar.getInstance();
    calendar.setTimeZone(TimeZone.getTimeZone("GMT"));
    calendar.setTime(microSecondDate);
    calendar.add(Calendar.DAY_OF_YEAR, getIncrementValue());
    microSecondDate = new MicroSecondDate(calendar.getTime());
    MicroSecondDate endDate = new MicroSecondDate(givenEndTime);
    if(endDate.before(microSecondDate)) return givenEndTime;
    return microSecondDate.getFissuresTime();

    }

    /**
     * checks to see if the eventArm is done processing for the time interval
     * specified in the configuration file.
     * If finished returns true else returns false.
     */

    private boolean isFinished(edu.iris.Fissures.Time endTime,
                   edu.iris.Fissures.Time givenEndTime) {

    if( (new MicroSecondDate(givenEndTime)).before( new MicroSecondDate(endTime)) ||
        (new MicroSecondDate(givenEndTime)).equals( new MicroSecondDate(endTime)))  {
        //return true;
        return  passivateEventArm(givenEndTime);
    }
    else return false;
    }

    /***
     * passivates the eventArm (makes it to sleep) based on the refreshInterval
     * and quitTime specified in the property file.
     */

    private boolean passivateEventArm(edu.iris.Fissures.Time endTime) {
    //if(turn == 1) System.exit(0);
    TimeInterval timeInterval = new TimeInterval(Start.QUIT_TIME,
                             UnitImpl.DAY);
    MicroSecondDate endDate = new MicroSecondDate(endTime);
    MicroSecondDate quitDate = endDate.add(timeInterval);
    MicroSecondDate currentDate = new MicroSecondDate();
    if(quitDate.before(currentDate))  return true;
    try {
        logger.debug("Sleep before looking for new events, will sleep for "+
                     Start.REFRESH_INTERVAL+" seconds");
        if(turn != 0)
        Thread.sleep(Start.REFRESH_INTERVAL * 1000);
    } catch(InterruptedException ie) {
        logger.warn("Event arm sleep was interrupted.", ie);
    }
    Start.getEventQueue().deleteTimeConfig();
    turn++;
    return false;
    }

    /**
     * returns the increment value. This value is specified as a property in the
     * property file.
     */

    private int getIncrementValue() {
    String value = props.getProperty("edu.sc.seis.sod.daystoincrement");
    if(value == null) return 1;
    try {
        int val = Integer.parseInt(value);
        return val;
    } catch(NumberFormatException nfe) {
        return 1;
    }
    }

    private static int turn = 0;
    private edu.sc.seis.sod.subsetter.eventArm.EventFinder eventFinderSubsetter;

    private edu.sc.seis.sod.subsetter.eventArm.EventChannelFinder eventChannelFinder = null;

    private edu.sc.seis.sod.EventAttrSubsetter eventAttrSubsetter = new NullEventAttrSubsetter();

    private OriginSubsetter originSubsetter = new NullOriginSubsetter();

    private EventArmProcess eventArmProcess = new NullEventProcess();

    private edu.iris.Fissures.IfEvent.EventFinder finder = null;

    private Element config = null;

    private Properties props;

    static Category logger =
        Category.getInstance(EventArm.class.getName());
}// EventArm
