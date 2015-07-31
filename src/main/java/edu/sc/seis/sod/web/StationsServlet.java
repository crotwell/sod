package edu.sc.seis.sod.web;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import edu.iris.Fissures.network.StationImpl;
import edu.sc.seis.fissuresUtil.hibernate.NetworkDB;


public class StationsServlet extends HttpServlet {

    public StationsServlet() {
        // TODO Auto-generated constructor stub
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String URL = req.getRequestURL().toString();
        System.out.println("GET: "+URL);
        resp.setContentType("text/html");
        resp.setStatus(HttpServletResponse.SC_OK);
        PrintWriter out = resp.getWriter();
        out.println("<html><body>");
        out.println("<h1>Hello from Station Servlet</h1>");
        StationImpl[] stations = NetworkDB.getSingleton().getAllStations();
        out.println("<ul>");
        for (StationImpl sta : stations) {
            out.println("<li>"+sta.toString()+"</li>");
        }
        out.println("</ul>");

        out.println("</body></html>");
        NetworkDB.rollback();
    }
}
