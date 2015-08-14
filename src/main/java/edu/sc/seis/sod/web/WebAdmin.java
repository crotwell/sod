package edu.sc.seis.sod.web;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.DefaultHandler;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.servlet.ServletHandler;

import edu.sc.seis.sod.Arm;
import edu.sc.seis.sod.ArmListener;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.RunProperties;
import edu.sc.seis.sod.Start;


public class WebAdmin implements ArmListener{

    public WebAdmin() {
        // TODO Auto-generated constructor stub
    }
    
    public void start() throws Exception
    {
        // Create a basic Jetty server object that will listen on port 8080.  Note that if you set this to port 0
        // then a randomly available port will be assigned that you can either look in the logs for the port,
        // or programmatically obtain it for use in test cases.
        server = new Server(8080);
 
        List<Handler> handlerList = new ArrayList<Handler>();
        
        // Create the ResourceHandler. It is the object that will actually handle the request for a given file. It is
        // a Jetty Handler object so it is suitable for chaining with other handlers as you will see in other examples.
        ResourceHandler resource_handler = new ResourceHandler();
        // Configure the ResourceHandler. Setting the resource base indicates where the files should be served out of.
        // In this example it is the current directory but it can be configured to anything that the jvm has access to.
        resource_handler.setDirectoriesListed(true);
        resource_handler.setWelcomeFiles(new String[]{ "index.html" });
        resource_handler.setResourceBase("site");
        ContextHandler siteContext = new ContextHandler("/site");
        siteContext.setHandler(resource_handler);
        handlerList.add(siteContext);
        
        ServletHandler servlets = new ServletHandler();
        servlets.addServletWithMapping(ArmStatusServlet.class, "/api/arms");
        servlets.addServletWithMapping(EventServlet.class, "/api/events");
        servlets.addServletWithMapping(EventServlet.class, "/api/events/");
        servlets.addServletWithMapping(EventServlet.class, "/api/events/*");
        servlets.addServletWithMapping(NetworkServlet.class, "/api/networks");
        servlets.addServletWithMapping(NetworkServlet.class, "/api/networks/");
        servlets.addServletWithMapping(NetworkServlet.class, "/api/networks/*");
        servlets.addServletWithMapping(StationsServlet.class, "/api/stations");
        servlets.addServletWithMapping(StationsServlet.class, "/api/stations/");
        servlets.addServletWithMapping(StationsServlet.class, "/api/stations/*");

        servlets.addServletWithMapping(WaveformServlet.class, "/api/waveform");
        servlets.addServletWithMapping(WaveformServlet.class, "/api/waveform/");
        servlets.addServletWithMapping(WaveformServlet.class, "/api/waveform/*");

        servlets.addServletWithMapping(EventStationServlet.class, "/api/eventstations");
        servlets.addServletWithMapping(EventStationServlet.class, "/api/eventstations/");
        servlets.addServletWithMapping(EventStationServlet.class, "/api/eventstations/*");
 
        // Add the ResourceHandler to the server.
        HandlerList handlers = new HandlerList();
        handlers.setHandlers(new Handler[] { servlets, siteContext, new DefaultHandler() {

            @Override
            public void handle(String target,
                               Request baseRequest,
                               HttpServletRequest request,
                               HttpServletResponse response) throws IOException, ServletException {
                System.out.println("missed get: "+request.getRequestURL());
                super.handle(target, baseRequest, request, response);
            }
            
        },
        new DefaultHandler() });
        server.setHandler(handlers);
 
        // Start things up! By using the server.join() the server thread will join with the current thread.
        // See "http://docs.oracle.com/javase/1.5.0/docs/api/java/lang/Thread.html#join()" for more details.
        server.start();
        final Server serverInContext = server;
        //server.join();  // this means web server quits when current thread quits
        logger.info("Web Admin started at "+server.getConnectors()[0].getName());
        System.out.println("Web Admin started at "+server.getConnectors()[0].getName());
        /*
        if ( Start.getRunProps().isStatusWebKeepAlive() ) {
            keepAliveThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        System.err.println("Before join jetty");
                        serverInContext.join();
                        System.err.println("After join, guess jetty quit");
                    } catch(InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
                
            }, "JettyKeepAlive");
            keepAliveThread.start();
        }
        */
    }

    @Override
    public void finished(Arm arm) {
        if ( ! Start.getRunProps().isStatusWebKeepAlive() &&  ! Start.isAnyWaveformArmActive()) {
            try {
                System.err.println("All finished, stopping web admin");
                server.stop();
            } catch(Exception e) {
                logger.error("Unable to stop jetty web server", e);
            }
        }
    }

    @Override
    public void starting(Arm arm) throws ConfigurationException {
    }

    @Override
    public void started() throws ConfigurationException {
    }
    
    Thread keepAliveThread;
    
    Server server;
    
    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(WebAdmin.class);

    public void join() throws InterruptedException {
        System.err.println("Before Join");
        server.join();
        System.err.println("After Join");
    }

    public static String getBaseUrl() {
        // TODO Auto-generated method stub
        return "/api";
    }
}
