package edu.sc.seis.sod.subsetter.waveFormArm;

import edu.sc.seis.sod.*;
import edu.sc.seis.sod.database.*;

import edu.iris.Fissures.IfEvent.*;
import edu.iris.Fissures.IfNetwork.*;
import edu.iris.Fissures.network.*;

import org.w3c.dom.*;
import java.io.*;


/**
 * WaveformStatusProcess.java
 *
 *
 * Created: Fri Oct 18 14:57:48 2002
 *
 * @author <a href="mailto:">Srinivasa Telukutla</a>
 * @version
 */

public class WaveformStatusProcess implements Subsetter{
    public WaveformStatusProcess (Element config){
	fileName = SodUtil.getNestedText(config);
// 	try {
// 	    FileWriter fw = new FileWriter(fileName);
// 	    bw = new BufferedWriter(fw);
// 	    writeHeader();
// 	} catch(Exception e) {
// 	    e.printStackTrace();
// 	}
    }

    private void writeHeader() {
	String header = "<html><head><title>sodReport</title></head><body>";
	write(header);
	//	newLine();
	
    }

    private void writeTail() {
	String str = "</body></html>";
	write(str);
    }

    private void startTable() {
	String str = new String("<table>");
	write(str);
	//bw.newLine();
    }

    private void endTable() {
	String str = new String("</table>");
	write(str);
	//bw.newLine();
    }

    private void write(String str) {
	try {
	    FileWriter fw = new FileWriter(fileName, true);
	    BufferedWriter bw = new BufferedWriter(fw);
	    bw.write(str, 0, str.length());
	    bw.write("<br><br>");
	    bw.newLine();
	    bw.close();
	    fw.close();
	} catch(Exception e) {
	    e.printStackTrace();
	}
    }
    public void begin(EventAccessOperations eventAccess) {
	totalEvents++;
	EventAttr eventAttr = eventAccess.get_attributes();
	startTable();
	write("<tr> Processing Event "+eventAttr.name+"</tr>");
    }
    public void begin(EventAccessOperations eventAccess, NetworkAccess networkAccess) {
	NetworkAttr networkAttr = networkAccess.get_attributes();
	write("<tr> Processing Network "+networkAttr.name+"</tr>");
	//bw.newLine();
    }

    public void begin(EventAccessOperations eventAccess, Station station) {
// 	write("<tr> Processing Station "+station.get_code()+"</tr>");
    }
    
    public void begin(EventAccessOperations eventAccess, Site site) {
// 	write("<tr> Processing Site "+site.get_code()+"</tr>");
    }
    
    public void begin(EventAccessOperations eventAccess, Channel channel) {
       write("<tr> Processing Channel "+ChannelIdUtil.toString(channel.get_id())+"</tr>");
    }

    public void end(EventAccessOperations eventAccess, Channel channel, Status status, String reason) {
	write("<tr> Done with Channel "+ChannelIdUtil.toString(channel.get_id())+" Status: "+status.toString()+
		 "Reason: "+reason+" </tr>");
    }
    
    public void end(EventAccessOperations eventAccess, Site site) {
// 	write("<tr> Done with Site "+site.get_code()+"</tr>");
    }
    
    public void end(EventAccessOperations eventAccess, Station station) {
	//	write("<tr> Done with Station "+station.get_code()+"</tr>");
    }

    public void end(EventAccessOperations eventAccess, NetworkAccess networkAccess) {
	//	write("<tr> Processing Station "+station.get_code()+"</tr>");
	NetworkAttr networkAttr = networkAccess.get_attributes();
	write("<tr> Done with Network "+networkAttr.name+"</tr>");
    }

    public void end(EventAccessOperations eventAccess) {
	EventAttr eventAttr = eventAccess.get_attributes();
	write("<tr> Done with Event "+eventAttr.name+"</tr>");
	endTable();
    }

    public void closeProcessing() {
	write("<h1> Total number of events processed are "+totalEvents+"</h1>");
	writeTail();
    }

    private String fileName = new String("status.html");
    private int totalEvents = 0;
  
}// WaveformStatusProcess
