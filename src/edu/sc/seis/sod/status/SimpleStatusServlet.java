package edu.sc.seis.sod.status;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.mortbay.http.HttpContext;
import org.mortbay.http.HttpServer;
import org.mortbay.http.SocketListener;
import org.mortbay.http.handler.ResourceHandler;
import org.mortbay.jetty.servlet.ServletHandler;
import org.mortbay.util.MultiException;
import edu.sc.seis.fissuresUtil.exceptionHandler.GlobalExceptionHandler;
import edu.sc.seis.sod.Start;
import edu.sc.seis.sod.status.eventArm.EventStatusTemplate;

/**
 * @author groves Created on Aug 16, 2004
 */
public class SimpleStatusServlet extends HttpServlet {
    
    public static void addLoc(String location, GenericTemplate template) {
        servlets.addServlet("/"+location, "edu.sc.seis.sod.status.SimpleStatusServlet");
        locs.put("/"+location,template);
    }

    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response) throws IOException {
        GenericTemplate template = (GenericTemplate)locs.get(request.getServletPath());
        response.getOutputStream().print(template.getResult());
        response.getOutputStream().close();
    }
    
    private EventStatusTemplate eventStatus;
    private static Map locs = new HashMap();

    private static ServletHandler servlets = new ServletHandler();

    private static HttpServer server;
    static {
        server = new HttpServer();
        SocketListener listener = new SocketListener();
        listener.setPort(8081);
        try {
            listener.setHost("pokey.seis.sc.edu");
        } catch(UnknownHostException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        server.addListener(listener);
        HttpContext context = new HttpContext();
        context.setContextPath("/");
        server.addContext(context);
        context.addHandler(servlets);
        context.setResourceBase("./" + Start.getRunProps().getStatusBaseDir());
        context.addHandler(new ResourceHandler());
        try {
            server.start();
        } catch(MultiException e) {
            GlobalExceptionHandler.handle(e);
        }
    }
}