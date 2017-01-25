package edu.sc.seis.sod.web;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.BindException;

import javax.servlet.Servlet;
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
import org.eclipse.jetty.util.resource.Resource;

import edu.sc.seis.sod.Arm;
import edu.sc.seis.sod.ArmListener;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.Start;


public class WebAdmin implements ArmListener{

    public static final String SITE = "site";
    public static final String API = "api";

    public WebAdmin() {
        // TODO Auto-generated constructor stub
    }
    
    public void start() throws Exception
    {
        
        
        // Create the ResourceHandler. It is the object that will actually handle the request for a given file. It is
        // a Jetty Handler object so it is suitable for chaining with other handlers as you will see in other examples.
        ResourceHandler resource_handler = new ResourceHandler();
        // Configure the ResourceHandler. Setting the resource base indicates where the files should be served out of.
        // In this example it is the current directory but it can be configured to anything that the jvm has access to.
        resource_handler.setDirectoriesListed(true);
        resource_handler.setWelcomeFiles(new String[]{ "index.html" });
        resource_handler.setBaseResource(Resource.newResource(new File(SITE)));
        ContextHandler siteContext = new ContextHandler("/");
        siteContext.setHandler(resource_handler);
        
        ServletHandler servlets = new ServletHandler();
        servlets.setEnsureDefaultServlet(false);
        servlets.addServletWithMapping(ArmStatusServlet.class, "/"+API+"/"+"arms");

        addServlets(servlets, EventServlet.class, "quakes" );
        addServlets(servlets, NetworkServlet.class, "networks" );
        addServlets(servlets, StationsServlet.class, "stations" );
        addServlets(servlets, ChannelServlet.class, "channels" );
        addServlets(servlets, EventStationServlet.class, "quake-stations" );
        addServlets(servlets, EventVectorServlet.class, "quake-vectors" );
        addServlets(servlets, WaveformServlet.class, "waveform" );
        addServlets(servlets, WaveformServlet.class, "waveforms" );
        addServlets(servlets, TauPServlet.class, "taup");
        addServlets(servlets, SodConfigServlet.class, "sod-configs");
        
        // Add the ResourceHandler to the server.
        HandlerList handlers = new HandlerList();
        handlers.setHandlers(new Handler[] { siteContext, servlets, new AlwaysEmberIndexHandler(), new DefaultHandler() {

            @Override
            public void handle(String target,
                               Request baseRequest,
                               HttpServletRequest request,
                               HttpServletResponse response) throws IOException, ServletException {
                logger.warn("missed get: "+request.getRequestURL());
                super.handle(target, baseRequest, request, response);
            }
            
        },
        new DefaultHandler() });
        
     // Create a basic Jetty server object that will listen on port 8080.  Note that if you set this to port 0
        // then a randomly available port will be assigned that you can either look in the logs for the port,
        // or programmatically obtain it for use in test cases.
        int initialPort = 8080;
        int port = initialPort;
        int maxPortTries = 10;
        while(server == null) {
            try {
                server = new Server(port);
                server.setHandler(handlers);
 
        // Start things up! By using the server.join() the server thread will join with the current thread.
        // See "http://docs.oracle.com/javase/1.5.0/docs/api/java/lang/Thread.html#join()" for more details.
                server.start();
            } catch(BindException e) {
                if (port-initialPort < maxPortTries) {
                    logger.info("port "+port+" in use, trying next in line.", e.getMessage());
                    port++;
                } else {
                    throw e;
                }
            }
        }
        final Server serverInContext = server;
        //server.join();  // this means web server quits when current thread quits
        logger.info("Web Admin started at "+server.getURI());
        System.out.println("Web Admin started at "+server.getURI());
        if ( Start.getArgs().isStatus() || Start.getRunProps().isStatusWebKeepAlive() ) {
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
        
    }
    
    void addServlets(ServletHandler servlets, Class<? extends Servlet> servletClass, String partialUrl) {
        servlets.addServletWithMapping(servletClass, "/"+API+"/"+partialUrl);
        servlets.addServletWithMapping(servletClass, "/"+API+"/"+partialUrl+"/");
        servlets.addServletWithMapping(servletClass, "/"+API+"/"+partialUrl+"/*");
    }

    
    
    @Override
    public void finished(Arm arm) {
        checkIfStayAlive();
    }

    @Override
    public void starting(Arm arm) throws ConfigurationException {
        
    }

    @Override
    public void started() throws ConfigurationException {
        // do this here in case SOD is already done on startup, so no arms actually finish
        // and if we don't check we will stay alive forever even if we shouldn't
        checkIfStayAlive();
    }
    
    void checkIfStayAlive() {
        System.err.println("check if stay alive");
        if (! Start.isAnyWaveformArmActive()) {
            // last arm has finished
            if ( Start.getRunProps().isStatusWebKeepAlive() ) {
                System.err.println("--status to keep SOD alive true, so control-c to quit SOD.");
            } else {
                try {
                    System.err.println("All finished, stopping web admin");
                    server.stop();
                } catch(Exception e) {
                    logger.error("Unable to stop jetty web server", e);
                }
            }
        }
    }
    
    Thread keepAliveThread;
    
    Server server;
    
    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(WebAdmin.class);

    public void join() throws InterruptedException {
        System.err.println("Before Join");
        server.join();
        System.err.println("After Join");
    }

    public static String getApiBaseUrl() {
        return "/"+API;
    }
}
