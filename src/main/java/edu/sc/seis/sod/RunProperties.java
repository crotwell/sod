/**
 * RunProperties.java
 * 
 * @author Charles Groves
 */
package edu.sc.seis.sod;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Element;

import edu.iris.Fissures.model.TimeInterval;
import edu.iris.Fissures.model.UnitImpl;
import edu.sc.seis.fissuresUtil.display.configuration.DOMHelper;
import edu.sc.seis.sod.source.event.AbstractEventSource;

public class RunProperties {

	public RunProperties() throws ConfigurationException {
	}

	public void addProperties(Element el) throws ConfigurationException {
		if (el != null) {
			Element runNameChild = SodUtil.getElement(el, "runName");
			if (runNameChild != null) {
				runName = SodUtil.getText(runNameChild);
			}
			Element statusBaseChild = SodUtil.getElement(el, "statusBase");
			if (statusBaseChild != null) {
				statusDir = SodUtil.getText(statusBaseChild);
			}
			Element eventQueryChild = SodUtil.getElement(el,
					AbstractEventSource.EVENT_QUERY_INCREMENT);
			if (eventQueryChild != null) {
				eventQueryIncrement = SodUtil.loadTimeInterval(eventQueryChild);
			}
			Element eventLagChild = SodUtil.getElement(el, AbstractEventSource.EVENT_LAG);
			if (eventLagChild != null) {
				eventLag = SodUtil.loadTimeInterval(eventLagChild);
			}
			Element eventRefreshChild = SodUtil.getElement(el,
			                                               AbstractEventSource.EVENT_REFRESH_INTERVAL);
			if (eventRefreshChild != null) {
				eventRefresh = SodUtil.loadTimeInterval(eventRefreshChild);
			}
			Element maxRetryChild = SodUtil.getElement(el, "maxRetryDelay");
			if (maxRetryChild != null) {
				maxRetry = SodUtil.loadTimeInterval(maxRetryChild);
			}
			Element seismogramLatencyEl = SodUtil.getElement(el,
					"seismogramLag");
			if (seismogramLatencyEl != null) {
				seismogramLatency = SodUtil
						.loadTimeInterval(seismogramLatencyEl);
			}
			Element serverRetryChild = SodUtil.getElement(el,
					"serverRetryDelay");
			if (serverRetryChild != null) {
				serverRetryDelay = SodUtil.loadTimeInterval(serverRetryChild);
			}
			Element numWorkersChild = SodUtil.getElement(el,
					"waveformWorkerThreads");
			if (numWorkersChild != null) {
				numWorkers = Integer.parseInt(SodUtil.getText(numWorkersChild));
			}
			Element evChanPairProcChild = SodUtil.getElement(el,
					"eventChannelPairProcessing");
			if (evChanPairProcChild != null) {
				evChanPairProc = SodUtil.getText(evChanPairProcChild);
			}
            Element chanGroupRuleChild = SodUtil.getElement(el,
                    "channelGroupingRules");
            if (chanGroupRuleChild != null) {
                channelGroupingRules = SodUtil.getText(chanGroupRuleChild);
            }
			if (SodUtil.isTrue(el, "reopenEvents", false)) {
				reopenEvents = true;
			}
			if (SodUtil.isTrue(el, "removeDatabase", false)) {
				removeDatabase = true;
			}
            if ( ! SodUtil.isTrue(el, "warnIfDatabaseExists", true)) {
                warnIfDatabaseExists = false;   // default is true
            }
			if (SodUtil.isTrue(el, "statusPages", false)) {
				statusPages = true;
			}
			if (DOMHelper.hasElement(el, "checkpointPeriodically")) {
				checkpointPeriodically = true;
			}
			if (DOMHelper.hasElement(el, "loserEventCleaner")) {
				loserEventCleaner = true;
			}
			if (DOMHelper
					.hasElement(el, "allowNetworksOutsideEventRequestTime")) {
				allowDeadNets = true;
			}
            if (DOMHelper
                    .hasElement(el, "skipAvailableData")) {
                skipAvailableData = DOMHelper.extractBoolean(el, "skipAvailableData");
            }
			Element hibernateExtraConfig = SodUtil.getElement(el, "hibernateConfig");
			if (hibernateExtraConfig != null) {
			    hibernateConfig.add(SodUtil.getText(hibernateExtraConfig));
			}
		}
	}

	public TimeInterval getMaxRetryDelay() {
		return maxRetry;
	}

	public TimeInterval getServerRetryDelay() {
		return serverRetryDelay;
	}

	public TimeInterval getEventQueryIncrement() {
		return eventQueryIncrement;
	}

	public TimeInterval getEventLag() {
		return eventLag;
	}

	public TimeInterval getEventRefreshInterval() {
		return eventRefresh;
	}

	public TimeInterval getSeismogramLatency() {
		return seismogramLatency;
	}

	public String getRunName() {
		return runName;
	}

	public String getStatusBaseDir() {
		return statusDir;
	}

	public int getNumWaveformWorkerThreads() {
		return numWorkers;
	}

	public boolean reopenEvents() {
		return reopenEvents;
	}

	public boolean removeDatabase() {
		return removeDatabase;
	}

    public boolean warnIfDatabaseExists() {
        return warnIfDatabaseExists;
    }

	public String getEventChannelPairProcessing() {
		return evChanPairProc;
	}

	public boolean reopenSuspended() {
		return !evChanPairProc.equals(DONT_RESTART);
	}

	public boolean doStatusPages() {
		return statusPages;
	}

	public boolean checkpointPeriodically() {
		return checkpointPeriodically;
	}

	public boolean loserEventCleaner() {
		return loserEventCleaner;
	}

	public void setAllowDeadNets(boolean b) {
		this.allowDeadNets = b;
	}

	public boolean allowDeadNets() {
		return allowDeadNets;
	}
	
    public boolean isSkipAvailableData() {
        return skipAvailableData;
    }

    public void setSkipAvailableData(boolean skipAvailableData) {
        this.skipAvailableData = skipAvailableData;
    }

    public List getHibernateConfig() {
	    return hibernateConfig;
	}
	
    public String getChannelGroupingRules() {
        return channelGroupingRules;
    }

    public static final TimeInterval NO_TIME = new TimeInterval(0,
			UnitImpl.SECOND);

	public static final TimeInterval ONE_WEEK = new TimeInterval(7,
			UnitImpl.DAY);

	public static final TimeInterval TEN_MIN = new TimeInterval(10,
			UnitImpl.MINUTE);

	public static final TimeInterval DAYS_180 = new TimeInterval(180,
			UnitImpl.DAY);

	private TimeInterval eventQueryIncrement = ONE_WEEK;

	private TimeInterval eventLag = ONE_WEEK;

	private TimeInterval eventRefresh = TEN_MIN;

	private TimeInterval maxRetry = DAYS_180;

	private TimeInterval serverRetryDelay = NO_TIME;

	private TimeInterval seismogramLatency = (TimeInterval) ONE_WEEK
			.multiplyBy(4);

	private String runName = "Your Sod";

	private String statusDir = "status";

	private int numWorkers = 1;

	private boolean reopenEvents = false;

    private boolean removeDatabase = false;
    
    private boolean warnIfDatabaseExists = true;

	private boolean statusPages = false;

	public static final String DONT_RESTART = "noCheck";

	public static final String AT_LEAST_ONCE = "atLeastOnce";

	public static final String AT_MOST_ONCE = "atMostOnce";

	private String evChanPairProc = AT_LEAST_ONCE;

	private boolean checkpointPeriodically = false;

	private boolean loserEventCleaner = false;

	private boolean allowDeadNets;
	
	private String channelGroupingRules = null;
	
	private boolean skipAvailableData = false;
	
	private List hibernateConfig = new ArrayList();
}