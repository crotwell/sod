package edu.sc.seis.sod;

import java.util.List;
import java.util.Properties;
import edu.sc.seis.fissuresUtil.exceptionHandler.ExceptionReporter;
import edu.sc.seis.fissuresUtil.exceptionHandler.ExceptionReporterUtils;

/**
 * The four key strings' SMTP, SUBJECT, FROM and TO values must be set in the
 * passed in properties
 */
public class MailExceptionReporter extends ResultMailer implements
        ExceptionReporter {

    public MailExceptionReporter(Properties props)
            throws ConfigurationException {
        super(props);
        if(props.containsKey(LIMIT)) {
            limit = Integer.parseInt(props.getProperty(LIMIT));
        }
    }

    public void report(String message, Throwable e, List sections)
            throws Exception {
        if(numSent < limit) {
            numSent++;
            mail(message, ExceptionReporterUtils.getTrace(e), sections);
        } else {
            logger.debug("Not sending an email since " + numSent
                    + " have been sent and " + limit
                    + " is the max number to send");
        }
    }

    /**
     * mail.limit specifies the number of emails to send
     */
    public static final String LIMIT = "mail.limit";

    private int numSent = 0;

    private int limit = Integer.MAX_VALUE;

    private static final org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(MailExceptionReporter.class);
}
