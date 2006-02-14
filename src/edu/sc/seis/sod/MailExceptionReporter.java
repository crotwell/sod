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
    }

    public void report(String message, Throwable e, List sections)
            throws Exception {
        mail(message, ExceptionReporterUtils.getTrace(e), sections);
    }
}
