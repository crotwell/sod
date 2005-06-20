package edu.sc.seis.sod;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.Address;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import org.apache.log4j.BasicConfigurator;
import edu.sc.seis.fissuresUtil.exceptionHandler.ExceptionReporter;
import edu.sc.seis.fissuresUtil.exceptionHandler.ExceptionReporterUtils;
import edu.sc.seis.fissuresUtil.exceptionHandler.GlobalExceptionHandler;
import edu.sc.seis.fissuresUtil.exceptionHandler.Section;

/**
 * The four key strings' SMTP, SUBJECT, FROM and TO values must be set in the
 * passed in properties
 */
public class MailExceptionReporter implements ExceptionReporter {

    public MailExceptionReporter(Properties props)
            throws ConfigurationException {
        this.props = props;
        checkProperties();
        logger.info("Exception mailer going to " + props.getProperty(TO)
                + " from " + props.getProperty(FROM) + " through "
                + props.getProperty(SMTP) + " created");
        if(props.containsKey(LIMIT)) {
            limit = Integer.parseInt(props.getProperty(LIMIT));
        }
    }

    private void checkProperties() throws ConfigurationException {
        checkProperty(SMTP);
        checkProperty(SUBJECT);
        checkProperty(TO);
        checkProperty(FROM);
    }

    private void checkProperty(String property) throws ConfigurationException {
        if(props.getProperty(property) == null) {
            throw new ConfigurationException("A system properties required by this class isn't set! "
                    + property + " must be set");
        }
    }

    public void report(String message, Throwable e, List sections)
            throws Exception {
        if(numSent < limit) {
            numSent++;
            Session session = Session.getDefaultInstance(props, null);
            Message msg = new MimeMessage(session);
            InternetAddress addressFrom = new InternetAddress(props.getProperty(FROM));
            msg.setFrom(addressFrom);
            Address addressTo = new InternetAddress(props.getProperty(TO));
            msg.setRecipient(Message.RecipientType.TO, addressTo);
            String subject = props.getProperty(SUBJECT) + " " + message;
            msg.setSubject(subject);
            Multipart multipart = new MimeMultipart();
            BodyPart bodyPart = new MimeBodyPart();
            bodyPart.setText(message + "\n"
                    + ExceptionReporterUtils.getTrace(e));
            multipart.addBodyPart(bodyPart);
            Iterator it = sections.iterator();
            while(it.hasNext()) {
                multipart.addBodyPart(createAttachement((Section)it.next()));
            }
            msg.setContent(multipart);
            Transport.send(msg);
        } else {
            logger.debug("Not sending an email since " + numSent
                    + " have been sent and " + limit
                    + " is the max number to send");
        }
    }

    private BodyPart createAttachement(Section section) throws IOException,
            MessagingException {
        String dir = System.getProperty("java.io.tmpdir");
        File file = new File(dir + section.getName() + ".txt");
        BufferedWriter bw = new BufferedWriter(new FileWriter(file));
        bw.write(section.getContents());
        bw.close();
        DataSource source = new FileDataSource(file);
        BodyPart bp = new MimeBodyPart();
        bp.setDataHandler(new DataHandler(source));
        bp.setFileName(section.getName() + ".txt");
        return bp;
    }

    /**
     * mail.smtp.host specifies the smtp server you want to use
     */
    public static final String SMTP = "mail.smtp.host";

    /**
     * mail.to specifies the recipient of the exception email
     */
    public static final String TO = "mail.to";

    /**
     * mail.from specifies the sender of the exception email
     */
    public static final String FROM = "mail.from";

    /**
     * mail.subject specifies the subject of the exception email
     */
    public static final String SUBJECT = "mail.subject";

    /**
     * mail.limit specifies the number of emails to send
     */
    public static final String LIMIT = "mail.limit";

    private Properties props;

    private int numSent = 0;

    private int limit = Integer.MAX_VALUE;

    private static final org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(MailExceptionReporter.class);

    public static void main(String[] args) throws ConfigurationException {
        BasicConfigurator.configure();
        Properties props = new Properties();
        System.out.println("ADDING PROPS");
        props.put(SUBJECT, "MailReporterTest");
        props.put(SMTP, "mail.seis.sc.edu");
        props.put(FROM, "exception@seis.sc.edu");
        props.put(TO, "groves@seis.sc.edu");
        System.out.println("CREATING MAIL REPORTER");
        GlobalExceptionHandler.add(new MailExceptionReporter(props));
        System.out.println("SENDING MAIL");
        GlobalExceptionHandler.handle("This is a test of the emergency brodcast system",
                                      new Exception("This is only a test"));
    }
}
