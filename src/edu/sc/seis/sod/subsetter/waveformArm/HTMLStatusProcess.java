package edu.sc.seis.sod.subsetter.waveFormArm;

import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfEvent.EventAttr;
import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.IfNetwork.NetworkAccess;
import edu.iris.Fissures.IfNetwork.NetworkAttr;
import edu.iris.Fissures.IfNetwork.Site;
import edu.iris.Fissures.IfNetwork.Station;
import edu.iris.Fissures.network.ChannelIdUtil;
import edu.sc.seis.sod.SodUtil;
import edu.sc.seis.sod.database.Status;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import org.w3c.dom.Element;


/**
 * HTMLStatusProcess.java
 *
 *
 * Created: Fri Oct 18 14:57:48 2002
 *
 * @author <a href="mailto:">Srinivasa Telukutla</a>
 * @version
 */

public class HTMLStatusProcess implements WaveformStatusProcess {
    public HTMLStatusProcess (Element config){
        fileName = SodUtil.getNestedText(config);

        //  try {
        //      FileWriter fw = new FileWriter(fileName);
        //      bw = new BufferedWriter(fw);
        //      writeHeader();
        //  } catch(Exception e) {
        //      e.printStackTrace();
        //  }
    }

    private void writeHeader() throws IOException {
        String header = "<html><head><title>sodReport</title></head><body bgcolor='#ffffff'>";
        write(header);
        //  newLine();

    }

    private void writeTail() throws IOException {
        String str = "</body></html>";
        write(str);
    }

    private void startTable() throws IOException {
        String str = new String("<table border='1' width='90%'>");
        write(str);
        //bw.newLine();
    }

    private void endTable() throws IOException {
        String str = new String("</table>");
        write(str);
        //bw.newLine();
    }

    private void write(String str) throws IOException {
        if (bw == null) {
            bw = new BufferedWriter(new FileWriter(fileName, true));
        } // end of if ()

        bw.write(str);
        bw.write("<br><br>");
        bw.newLine();
        bw.flush();
    }
    public void begin(EventAccessOperations eventAccess) throws IOException {
        totalEvents++;
        EventAttr eventAttr = eventAccess.get_attributes();
        startTable();
        write("<tr> Processing Event "+eventAttr.name+"</tr>");
    }
    public void begin(EventAccessOperations eventAccess, NetworkAccess networkAccess) throws IOException {
        NetworkAttr networkAttr = networkAccess.get_attributes();
        write("<tr> Processing Network "+networkAttr.name+"</tr>");
        //bw.newLine();
    }

    public void begin(EventAccessOperations eventAccess, Station station) {
        //  write("<tr> Processing Station "+station.get_code()+"</tr>");
    }

    public void begin(EventAccessOperations eventAccess, Site site) {
        //  write("<tr> Processing Site "+site.get_code()+"</tr>");
    }

    public void begin(EventAccessOperations eventAccess, Channel channel) throws IOException {
        write("<tr> Processing Channel "+ChannelIdUtil.toString(channel.get_id())+"</tr>");
    }

    public void end(EventAccessOperations eventAccess, Channel channel, Status status, String reason) throws IOException {
        write("<tr> Done with Channel "+ChannelIdUtil.toString(channel.get_id())+" Status: "+status.toString()+
                  "Reason: "+reason+" </tr>");
    }

    public void end(EventAccessOperations eventAccess, Site site) {
        //  write("<tr> Done with Site "+site.get_code()+"</tr>");
    }

    public void end(EventAccessOperations eventAccess, Station station) {
        //  write("<tr> Done with Station "+station.get_code()+"</tr>");
    }

    public void end(EventAccessOperations eventAccess, NetworkAccess networkAccess) throws IOException {
        //  write("<tr> Processing Station "+station.get_code()+"</tr>");
        NetworkAttr networkAttr = networkAccess.get_attributes();
        write("<tr> Done with Network "+networkAttr.name+"</tr>");
    }

    public void end(EventAccessOperations eventAccess) throws IOException {
        EventAttr eventAttr = eventAccess.get_attributes();
        write("<tr> Done with Event "+eventAttr.name+"</tr>");
        endTable();
    }

    public void closeProcessing() throws IOException {
        write("<h1> Total number of events processed are "+totalEvents+"</h1>");
        writeTail();
        try {
            bw.close();
            bw = null;
        } catch (IOException e) {
            e.printStackTrace();
        } // end of try-catch

    }

    private String fileName = new String("status.html");
    private int totalEvents = 0;
    BufferedWriter bw;

}// WaveformStatusProcess
