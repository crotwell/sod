package edu.sc.seis.sod;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringBufferInputStream;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import javax.activation.DataHandler;
import javax.activation.DataSource;
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
import edu.sc.seis.fissuresUtil.exceptionHandler.Section;

public class ResultMailer {

    /**
     * The four key strings' SMTP, SUBJECT, FROM and TO values must be set in the
     * passed in properties
     */
    public ResultMailer(Properties props) throws ConfigurationException {
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


    public void mail(String message, String bodyText, List sections)
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
                    + bodyText);
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
        DataSource source = new SectionDataSource(section);
        BodyPart bp = new MimeBodyPart();
        bp.setDataHandler(new DataHandler(source));
        bp.setFileName(section.getName() + ".txt");
        return bp;
    }
    
    private int numSent = 0;

    private int limit = Integer.MAX_VALUE;

    private Properties props;
    
    private static final org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(ResultMailer.class);

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
    
    class SectionDataSource implements DataSource {

        SectionDataSource(Section s) {
            this.s = s;
        }

        public String getContentType() {
            return "text/plain";
        }

        public InputStream getInputStream() throws IOException {
            return new StringBufferInputStream(s.getContents());
        }

        public String getName() {
            return s.getName();
        }

        public OutputStream getOutputStream() throws IOException {
            throw new RuntimeException("getOutputStream() not impl");
        }

        Section s;
    }
}
