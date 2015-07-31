package edu.sc.seis.sod.web;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONException;
import org.json.JSONWriter;

import edu.iris.Fissures.model.QuantityImpl;
import edu.iris.Fissures.model.SamplingImpl;
import edu.iris.Fissures.model.UnitImpl;
import edu.iris.Fissures.network.ChannelImpl;
import edu.iris.Fissures.network.NetworkAttrImpl;
import edu.iris.Fissures.network.StationImpl;
import edu.sc.seis.fissuresUtil.database.NotFound;
import edu.sc.seis.fissuresUtil.hibernate.NetworkDB;

public class NetworkServlet extends HttpServlet {

    public NetworkServlet() {
        // TODO Auto-generated constructor stub
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            String URL = req.getRequestURL().toString();
            System.out.println("GET: " + URL);
            resp.setContentType("application/json");
            PrintWriter writer = resp.getWriter();
            JSONWriter out = new JSONWriter(writer);
            NetworkDB netdb = NetworkDB.getSingleton();
            out.object();
            Matcher matcher = allNetworkPattern.matcher(URL);
            if (matcher.matches()) {
                // all nets
                List<NetworkAttrImpl> netList = netdb.getAllNetworks();
                out.key("networks").array();
                for (NetworkAttrImpl net : netList) {
                    encodeJson(net, out);
                }
                out.endArray();
            } else {
                matcher = networkPattern.matcher(URL);
                if (matcher.matches()) {
                    String netCode = matcher.group(1);
                    NetworkAttrImpl n = netdb.getNetwork(Integer.parseInt(matcher.group(1)));
                    out.key("networks");
                    encodeJson(n, out);
                } else {
                    matcher = stationPattern.matcher(URL);
                    if (matcher.matches()) {
                        // logger.debug("station");
                        String netCode = matcher.group(1);
                        String staCode = matcher.group(2);
                        StationImpl sta = netdb.getStation(Integer.parseInt(matcher.group(2)));
                        out.key("stations");
                        encodeJson(sta, out);
                    }
                }
            }
            out.endObject();
            writer.close();
        } catch(JSONException e) {
            throw new ServletException(e);
        } catch(NumberFormatException e) {
            throw new ServletException(e);
        } catch(NotFound e) {
            throw new ServletException(e);
        }
    }

    private void encodeJson(NetworkAttrImpl networkAttrImpl, JSONWriter out) {
        out.object()
           .key("id")
           .value(networkAttrImpl.getDbid())
                .key("networkCode")
                .value(networkAttrImpl.getId().network_code)
                .key("startYear")
                .value(networkAttrImpl.getId().begin_time.date_time.substring(0, 4))
                .key("description")
                .value(networkAttrImpl.getDescription())
        .endObject();
    }

    private void encodeJson(StationImpl sta, JSONWriter out) {
        out.object()
          .key("id")
          .value(sta.getDbid())
                .key("stationCode")
                .value(sta.get_code())
                .key("network")
                .value(sta.getNetworkAttrImpl().getDbid())
                .key("latitude")
                .value(sta.getLocation().latitude)
                .key("longitude")
                .value(sta.getLocation().longitude)
                .key("elevation")
                .value(((QuantityImpl)sta.getLocation().elevation).getValue(UnitImpl.METER))
                .endObject();
    }

    private void encodeJson(ChannelImpl chan, JSONWriter out) {
        out.object()
          .key("id")
          .value(chan.getDbid())
                .key("siteCode")
                .value(chan.getSite().get_code())
                .key("channelCode")
                .value(chan.get_code())
                .key("station")
                .value(chan.getStationImpl().getDbid())
                .key("sps")
                .value(((SamplingImpl)chan.getSamplingInfo()).getFrequency())
                .key("latitude")
                .value(chan.getSite().getLocation().latitude)
                .key("longitude")
                .value(chan.getSite().getLocation().longitude)
                .key("elevation")
                .value(((QuantityImpl)chan.getSite().getLocation().elevation).getValue(UnitImpl.METER))
                .key("depth")
                .value(((QuantityImpl)chan.getSite().getLocation().depth).getValue(UnitImpl.METER))
                .endObject();
    }

    Pattern allNetworkPattern = Pattern.compile(".*/networks");

    Pattern networkPattern = Pattern.compile(".*/networks/([A-Z0-9]+)");

    Pattern stationPattern = Pattern.compile(".*/networks/([A-Z0-9]+).([A-Z0-9]+)");

    Pattern channelPattern = Pattern.compile(".*/networks/([A-Z0-9]+).([A-Z0-9]+).([A-Z0-9][A-Z0-9]).([A-Z0-9][A-Z0-9][A-Z0-9])");
}
