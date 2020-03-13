/**
 * RunProperties.java
 *
 * @author Charles Groves
 */
package edu.sc.seis.sod;

import java.time.Duration;
import java.time.temporal.ChronoField;
import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Element;

import edu.sc.seis.sod.model.common.UnitImpl;
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
					"seismogramLatency");
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
			Element proxyHostEl = SodUtil.getElement(el, "proxyHost");
			if (proxyHostEl != null) {
				proxyHost = SodUtil.getText(proxyHostEl);
			}
			Element proxyPortEl = SodUtil.getElement(el, "proxyPort");
			if (proxyPortEl != null) {
				proxyPort = SodUtil.loadInt(el, "proxyPort", -1);
			}
			Element proxySchemeEl = SodUtil.getElement(el, "proxyScheme");
			if (proxySchemeEl != null) {
				proxyScheme = SodUtil.getText(proxySchemeEl);
			}
		}
	}

	public Duration getMaxRetryDelay() {
		return maxRetry;
	}

	public Duration getServerRetryDelay() {
		return serverRetryDelay;
	}

	public Duration getEventQueryIncrement() {
		return eventQueryIncrement;
	}

	public Duration getEventLag() {
		return eventLag;
	}

	public Duration getEventRefreshInterval() {
		return eventRefresh;
	}

	public Duration getSeismogramLatency() {
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

    public void setNumWaveformWorkerThreads(int numWorkers) {
        this.numWorkers = numWorkers;
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

    public boolean isStatusWebKeepAlive() {
        return statusWebKeepAlive;
    }

    public void setStatusWebKeepAlive(boolean b) {
        statusWebKeepAlive = b;
    }

    public boolean isStatusUnsecure() {
        return statusUnsecure;
    }

    public void setStatusUnsecure(boolean b) {
        statusUnsecure = b;
    }
    
    public void setProxyHost(String proxyHost) {
        this.proxyHost = proxyHost;
    }

    public String getProxyHost() {
        return proxyHost;
    }

    public void setProxyPort(int proxyPort) {
        this.proxyPort = proxyPort;
    }

    public int getProxyPort() {
        if (proxyPort <= 0) {
          return 80;
        }
        return proxyPort;
    }

    public void setProxyScheme(String proxyScheme) {
        this.proxyScheme = proxyScheme;
    }

    public String getProxyScheme() {
        if (proxyScheme == null) {
          return "http";
        }
        return proxyScheme;
    }

    public static final Duration NO_TIME = Duration.ofNanos(0);

	public static final Duration ONE_WEEK = Duration.ofDays(7);

	public static final Duration TEN_MIN = Duration.ofMinutes(10);

	public static final Duration DAYS_180 = Duration.ofDays(180);

	private Duration eventQueryIncrement = ONE_WEEK;

	private Duration eventLag = ONE_WEEK;

	private Duration eventRefresh = TEN_MIN;

	private Duration maxRetry = DAYS_180;

	private Duration serverRetryDelay = NO_TIME;

	private Duration seismogramLatency = ONE_WEEK.multipliedBy(4);

	private String runName = "Your Sod";

	private String statusDir = "status";

	public static final int DEFAULT_NUM_WORKER_THREADS = 1;

	private int numWorkers = DEFAULT_NUM_WORKER_THREADS;

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

	private boolean skipAvailableData = true;
	
	private boolean statusWebKeepAlive = false;
	
	private boolean statusUnsecure = false;
	
	private List hibernateConfig = new ArrayList();

	private String proxyHost = null;

	private int proxyPort = -1;

	private String proxyScheme = null;

}
