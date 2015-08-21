package edu.sc.seis.sod.web;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONWriter;

import edu.iris.Fissures.network.ChannelImpl;
import edu.sc.seis.fissuresUtil.database.NotFound;
import edu.sc.seis.fissuresUtil.hibernate.NetworkDB;
import edu.sc.seis.sod.web.jsonapi.ChannelJson;
import edu.sc.seis.sod.web.jsonapi.JsonApi;

public class ChannelServlet extends HttpServlet {

    public ChannelServlet() {
        // TODO Auto-generated constructor stub
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String URL = req.getRequestURL().toString();
        System.out.println("GET: " + URL);
        resp.setContentType("application/vnd.api+json");
        PrintWriter writer = resp.getWriter();
        JSONWriter out = new JSONWriter(writer);
        NetworkDB netdb = NetworkDB.getSingleton();
        Matcher matcher = channelDbidPattern.matcher(URL);
        if (matcher.matches()) {
            String dbid = matcher.group(1);
            ChannelImpl chan = null;
            try {
                chan = netdb.getChannel(Integer.parseInt(dbid));
                if (chan != null) {
                    JsonApi.encodeJson(out, new ChannelJson(chan, WebAdmin.getBaseUrl()));
                    resp.setStatus(HttpServletResponse.SC_OK);
                } else {
                    JsonApi.encodeError(out, "Channel is null for dbid " + dbid);
                }
            } catch(NumberFormatException e) {
                JsonApi.encodeError(out, "NumberFormatException " + e.getMessage());
            } catch(NotFound e) {
                JsonApi.encodeError(out, "NotFound " + e.getMessage());
            }
            writer.close();
        } else {
            JsonApi.encodeError(out, "bad url for servlet: regex=" + channelDbidPattern.toString());
            writer.close();
            resp.sendError(500);
        }
        NetworkDB.rollback();
    }

    Pattern channelDbidPattern = Pattern.compile(".*/channels/([0-9]+)");
}
