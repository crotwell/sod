/**
 * LocalSeismogramTemplate.java
 *
 * @author Created by Philip Oliver-Paull
 */

package edu.sc.seis.sod.status.waveformArm;


import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.IfNetwork.Station;
import edu.sc.seis.fissuresUtil.exceptionHandler.GlobalExceptionHandler;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.CookieJar;
import edu.sc.seis.sod.Stage;
import edu.sc.seis.sod.Standing;
import edu.sc.seis.sod.Status;
import edu.sc.seis.sod.process.waveformArm.LocalSeismogramTemplateGenerator;
import edu.sc.seis.sod.status.ChannelGroupTemplate;
import edu.sc.seis.sod.status.EventFormatter;
import edu.sc.seis.sod.status.FileWritingTemplate;
import edu.sc.seis.sod.status.GenericTemplate;
import edu.sc.seis.sod.status.StationFormatter;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.exception.MethodInvocationException;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.w3c.dom.Element;

public class LocalSeismogramTemplate extends FileWritingTemplate {

    private EventAccessOperations event;
    private Station station;
    private CookieJar cookieJar;

    //I have every intention of getting rid of this as soon as possible
    private List channelListeners = new ArrayList();

    public LocalSeismogramTemplate(Element el, String baseDir, String outputLocation, EventAccessOperations event, Station sta, CookieJar cookieJar)
        throws IOException, ConfigurationException{
        super(baseDir, outputLocation);
        this.event = event;
        this.cookieJar = cookieJar;
        station = sta;
        parse(el);
        write();
    }

    public void update(Channel chan, CookieJar cookieJar) throws Exception{
        this.cookieJar = cookieJar;
        //I intend to do something fancier very soon
        Iterator it = channelListeners.iterator();
        while (it.hasNext()){
            ((ChannelGroupTemplate)it.next()).change(chan, Status.get(Stage.PROCESSOR,
                                                                      Standing.SUCCESS));
        }
        write();
    }

    /**
     * Override method getResult to insert velocity post processing.
     *
     */
    public String getResult() {
        String s = super.getResult();

        s = getVelocityResult(s, cookieJar);
        return s;
    }

    public static String getVelocityResult(String template, CookieJar cookieJar) {
        try {
            StringWriter out = new StringWriter();
            // the new VeocityContext "wrapper" is to help with a possible memory leak
            // due to velocity gathering introspection information,
            // see http://jakarta.apache.org/velocity/developer-guide.html#Other%20Context%20Issues
            boolean status = LocalSeismogramTemplateGenerator.getVelocity().evaluate(new VelocityContext(cookieJar.getContext()),
                                                                    out,
                                                                    "localSeismogramTemplate",
                                                                    template);
            template = out.toString();
        } catch (ParseErrorException e) {
            GlobalExceptionHandler.handle("Problem using Velocity", e);
        } catch (MethodInvocationException e) {
            GlobalExceptionHandler.handle("Problem using Velocity", e);
        } catch (ResourceNotFoundException e) {
            GlobalExceptionHandler.handle("Problem using Velocity", e);
        } catch (IOException e) {
            GlobalExceptionHandler.handle("Problem using Velocity", e);
        }

        return template;
    }


    /**if this class has an template for this tag, it creates it using the
     * passed in element and returns it.  Otherwise it returns null.
     */
    protected Object getTemplate(String tag, Element el)throws ConfigurationException {
        //I intend to change this channels thing to something a little more sophisticated
        if (tag.equals("channels")){
            ChannelGroupTemplate cgt = new ChannelGroupTemplate(el);
            channelListeners.add(cgt);
            return cgt;
        }
        if (tag.equals("event")){
            return new EventTemplate(el);
        }
        if (tag.equals("station")){
            return new MyStationTemplate(el);
        }
        return super.getTemplate(tag,el);
    }

    private class EventTemplate implements GenericTemplate{
        public EventTemplate(Element el) throws ConfigurationException {
            formatter = new EventFormatter(el);
        }

        public String getResult() { return formatter.getResult(event); }

        private EventFormatter formatter;
    }

    private class MyStationTemplate implements GenericTemplate{
        public MyStationTemplate(Element el) throws ConfigurationException {
            formatter = new StationFormatter(el);
        }

        public String getResult() { return formatter.getResult(station); }

        private StationFormatter formatter;
    }

}


