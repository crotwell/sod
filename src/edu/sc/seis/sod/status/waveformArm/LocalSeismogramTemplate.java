/**
 * LocalSeismogramTemplate.java
 *
 * @author Created by Philip Oliver-Paull
 */

package edu.sc.seis.sod.status.waveformArm;


import edu.sc.seis.sod.status.*;

import edu.sc.seis.fissuresUtil.exceptionHandler.GlobalExceptionHandler;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.CookieJar;
import edu.sc.seis.sod.process.waveformArm.LocalSeismogramTemplateGenerator;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.exception.MethodInvocationException;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;
import javax.xml.parsers.ParserConfigurationException;

public class LocalSeismogramTemplate extends Template{
    public LocalSeismogramTemplate(Element el, String baseDir)
        throws ConfigurationException{
        this.baseDir = baseDir;
        parse(el);
    }

    public void update(String outputLocation, CookieJar cj){
        String loc = baseDir + outputLocation;
        synchronized(toBeRendered){
            if (!toBeRendered.containsKey(loc)){
                toBeRendered.put(loc, cj);
                writer.actIfPeriodElapsed();
            }
        }
    }

    protected Object textTemplate(final String text) {
        return new GenericTemplate() {
            public String getResult() { return text; }
        };
    }

    public class Writer extends PeriodicAction{
        public void act() {
            CookieJar[] jars = new CookieJar[0];
            String[] fileLocs = new String[0];
            synchronized(toBeRendered){
                int numCookiesWaiting = toBeRendered.size();
                if(toBeRendered.size() > 0){
                    jars = new CookieJar[toBeRendered.size()];
                    fileLocs = new String[toBeRendered.size()];
                    Iterator it = toBeRendered.keySet().iterator();
                    while(it.hasNext()){
                        String loc= (String)it.next();
                        fileLocs[--numCookiesWaiting] = loc;
                        jars[numCookiesWaiting] = (CookieJar)toBeRendered.get(loc);
                    }
                    toBeRendered.clear();
                }
            }
            for (int i = 0; i < jars.length; i++) {
                FileWritingTemplate.write(fileLocs[i], getResult(jars[i]));
            }
        }
    }

    public String getResult(CookieJar cj) {
        StringBuffer buf = new StringBuffer();
        Iterator e = templates.iterator();
        while(e.hasNext()) {
            buf.append(((GenericTemplate)e.next()).getResult());
        }
        return getVelocityResult(buf.toString(), cj);
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
        if(tag.equals("menu")){
            try {
                return new MenuTemplate(TemplateFileLoader.getTemplate(el), baseDir + "/1/2/test.html", baseDir);
            } catch (Exception e) {
                GlobalExceptionHandler.handle("Problem getting template for Menu", e);
            }
        }
        return super.getTemplate(tag,el);
    }

    private Writer writer = new Writer();
    private Map toBeRendered = Collections.synchronizedMap(new HashMap());
    private String baseDir;
}
