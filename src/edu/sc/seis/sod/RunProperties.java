/**
 * RunProperties.java
 *
 * @author Charles Groves
 */

package edu.sc.seis.sod;

import org.w3c.dom.Element;
import edu.iris.Fissures.model.TimeInterval;
import edu.iris.Fissures.model.UnitImpl;

public class RunProperties{
    public RunProperties(Element el) throws ConfigurationException{
        Element runNameChild = SodUtil.getElement(el, "runName");
        if(runNameChild != null ){
            runName = SodUtil.getText(runNameChild);
            CookieJar.getCommonContext().put("runName", runName);
        }

        Element statusBaseChild = SodUtil.getElement(el, "statusBase");
        if(statusBaseChild != null){
            statusDir = SodUtil.getText(statusBaseChild);
        }

        Element eventQueryChild = SodUtil.getElement(el, "eventQueryIncrement");
        if(eventQueryChild != null){
            eventQueryIncrement = SodUtil.loadTimeInterval(eventQueryChild);
        }

        Element eventLagChild = SodUtil.getElement(el, "eventLag");
        if(eventLagChild != null){
            eventLag = SodUtil.loadTimeInterval(eventLagChild);
        }

        Element eventRefreshChild = SodUtil.getElement(el, "eventRefreshInterval");
        if(eventRefreshChild != null){
            eventRefresh = SodUtil.loadTimeInterval(eventRefreshChild);
        }

        Element maxRetryChild = SodUtil.getElement(el, "maxRetryDelay");
        if(maxRetryChild != null){
            maxRetry = SodUtil.loadTimeInterval(maxRetryChild);
        }

        Element serverRetryChild = SodUtil.getElement(el, "serverRetryDelay");
        if(serverRetryChild != null){
            serverRetryDelay = SodUtil.loadTimeInterval(serverRetryChild);
        }

        Element numWorkersChild = SodUtil.getElement(el, "waveformWorkerThreads");
        if(numWorkersChild != null){
            numWorkers = Integer.parseInt(SodUtil.getText(numWorkersChild));
        }

        Element evChanPairProcChild = SodUtil.getElement(el, "eventChannelPairProcessing");
        if(evChanPairProcChild != null){
            evChanPairProc = SodUtil.getText(evChanPairProcChild);
        }

        if(SodUtil.isTrue(el, "reopenEvents")){
            reopenEvents = true;
        }

        if(SodUtil.isTrue(el, "removeDatabase")){
            removeDatabase = true;
        }
        if(!SodUtil.isTrue(el, "makeIndexPage")){
            doIndex = false;
        }
    }

    public TimeInterval getMaxRetryDelay() { return maxRetry; }

    public TimeInterval getServerRetryDelay(){ return serverRetryDelay; }

    public TimeInterval getEventQueryIncrement() { return eventQueryIncrement; }

    public TimeInterval getEventLag() { return eventLag; }

    public TimeInterval getEventRefreshInterval() { return eventRefresh; }

    public String getRunName(){ return runName; }

    public String getStatusBaseDir(){ return statusDir; }

    public int getNumWaveformWorkerThreads(){ return numWorkers; }

    public boolean reopenEvents(){ return reopenEvents; }

    public boolean removeDatabase(){ return removeDatabase; }

    public String getEventChannelPairProcessing(){
        return evChanPairProc;
    }
    
    public boolean doIndex(){ return doIndex; }

    public static final TimeInterval NO_TIME = new TimeInterval(0, UnitImpl.SECOND);
    public static final TimeInterval ONE_WEEK = new TimeInterval(7, UnitImpl.DAY);
    public static final TimeInterval TEN_MIN = new TimeInterval(10, UnitImpl.MINUTE);
    public static final TimeInterval DAYS_180 = new TimeInterval(180, UnitImpl.DAY);

    private TimeInterval eventQueryIncrement = ONE_WEEK;
    private TimeInterval eventLag = ONE_WEEK;
    private TimeInterval eventRefresh = TEN_MIN;
    private TimeInterval maxRetry = DAYS_180;
    private TimeInterval serverRetryDelay = NO_TIME;

    private String runName = "Your Sod";
    private String statusDir = "status";

    private int numWorkers = 1;

    private boolean reopenEvents = false;
    private boolean removeDatabase = false;
    private boolean doIndex = true;

    public static final String AT_LEAST_ONCE = "atLeastOnce";
    public static final String AT_MOST_ONCE = "atMostOnce";

    private String evChanPairProc = AT_LEAST_ONCE;
}
