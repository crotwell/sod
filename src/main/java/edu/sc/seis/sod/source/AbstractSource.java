package edu.sc.seis.sod.source;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;

import edu.sc.seis.fissuresUtil.cache.ClassicRetryStrategy;
import edu.sc.seis.fissuresUtil.cache.RetryStrategy;
import edu.sc.seis.fissuresUtil.namingService.FissuresNamingService;
import edu.sc.seis.sod.CommonAccess;
import edu.sc.seis.sod.SodUtil;
import edu.sc.seis.sod.UserReportRetryStrategy;

public abstract class AbstractSource implements Source {

    public AbstractSource(String name) {
        this(name, -1);
    }

    public AbstractSource(String name, int retries) {
        this.name = name;
        retries = -1;
        retryStrategy = new ClassicRetryStrategy(retries);
    }

    public AbstractSource(Element config, String defaultName) {
        this(config, defaultName, -1);
    }

    public AbstractSource(Element config, String defaultName, int defaultRetries) {
        if (config.hasAttribute("name")) {
            name = config.getAttribute("name");
        } else {
            name = SodUtil.loadText(config, NAME_ELEMENT, defaultName);
        }
        retries = SodUtil.loadInt(config, RETRIES_ELEMENT, defaultRetries);
        retryStrategy = new UserReportRetryStrategy(getRetries());
    }

    /*
     * (non-Javadoc)
     * 
     * @see edu.sc.seis.sod.source.Source#getName()
     */
    @Override
    public String getName() {
        return name;
    }

    public int getRetries() {
        return retries;
    }

    public FissuresNamingService getFissuresNamingService() {
        return CommonAccess.getNameService();
    }

    public RetryStrategy getRetryStrategy() {
        return retryStrategy;
    }

    public void setRetryStrategy(RetryStrategy retryStrategy) {
        if (retryStrategy != null) {
            this.retryStrategy = retryStrategy;
        } else {
            throw new IllegalArgumentException("RetryStrategy cannot be null");
        }
    }

    public void appendToName(String suffix) {
        name += suffix;
    }
    
    protected String name;

    private int retries = -1;

    private RetryStrategy retryStrategy;

    public static final String NAME_ELEMENT = "name";

    public static final String RETRIES_ELEMENT = "retries";

    private static Logger logger = LoggerFactory.getLogger(AbstractSource.class);
}// AbstractSource
