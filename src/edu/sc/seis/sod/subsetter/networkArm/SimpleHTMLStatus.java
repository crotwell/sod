/**
 * SimpleHTMLStatus.java
 *
 * @author Created by Omnicore CodeGuide
 */

package edu.sc.seis.sod.subsetter.networkArm;

import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.IfNetwork.NetworkAccess;
import edu.iris.Fissures.IfNetwork.Site;
import edu.iris.Fissures.IfNetwork.Station;
import edu.iris.Fissures.network.ChannelIdUtil;
import edu.iris.Fissures.network.NetworkIdUtil;
import edu.iris.Fissures.network.SiteIdUtil;
import edu.iris.Fissures.network.StationIdUtil;
import edu.sc.seis.sod.NetworkStatus;
import edu.sc.seis.sod.RunStatus;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.w3c.dom.Element;

public class SimpleHTMLStatus implements NetworkStatus{
    public SimpleHTMLStatus(Element config){
        File htmlDir = new File("html");
        htmlDir.mkdir();
        pages[0] = statusPage = new HTMLPage("Network Arm Status", "status.html", htmlDir);
        pages[1] = sitePage = new HTMLPage("Sites", "sites.html", htmlDir);
        pages[2] = networkPage = new HTMLPage("Networks", "networks.html", htmlDir);
        pages[3] = stationPage = new HTMLPage("Stations", "stations.html", htmlDir);
        pages[4] = channelPage = new HTMLPage("Channels", "channel.html", htmlDir);
        statusPage.addLinks(pages);
        sitePage.addLinks(pages);
        networkPage.addLinks(pages);
        stationPage.addLinks(pages);
        channelPage.addLinks(pages);
    }
    
    public void setArmStatus(String status) {
        statusPage.clear("Status");
        statusPage.append("Status", "<b>Status: </b> " + status);
        for (int i = 0; i < pages.length; i++) {
            pages[i].write();
        }
    }
    
    public void change(NetworkAccess network, RunStatus status) {
        if(status == RunStatus.FAILED){
            networkPage.append("Failures", NetworkIdUtil.toString(network.get_attributes().get_id()));
        }else if(status == RunStatus.PASSED){
            networkPage.append("Successes", NetworkIdUtil.toString(network.get_attributes().get_id()));
        }
    }
    
    public void change(Station station, RunStatus status) {
        if(status == RunStatus.FAILED){
            stationPage.append("Failures", StationIdUtil.toString(station.get_id()));
        }else if(status == RunStatus.PASSED){
            stationPage.append("Successes", StationIdUtil.toString(station.get_id()));
        }
    }
    
    public void change(Site site, RunStatus status) {
        if(status == RunStatus.FAILED){
            sitePage.append("Failures", SiteIdUtil.toString(site.get_id()));
        }else if(status == RunStatus.PASSED){
            sitePage.append("Successes", SiteIdUtil.toString(site.get_id()));
        }
    }
    
    public void change(Channel channel, RunStatus status) {
        if(status == RunStatus.FAILED){
            channelPage.append("Failures", ChannelIdUtil.toString(channel.get_id()));
        }else if(status == RunStatus.PASSED){
            channelPage.append("Successes", ChannelIdUtil.toString(channel.get_id()));
        }
    }
    
    private HTMLPage channelPage, sitePage, networkPage, stationPage, statusPage;
    
    private HTMLPage[] pages = new HTMLPage[5];
    
    private class HTMLPage{
        public HTMLPage(String title, String fileName, File path){
            this.title = title;
            this.path = path;
            this.fileName = fileName;
        }
        
        public void append(String section, String text){
            if(sectionToContents.get(section) == null){
                sectionToContents.put(section, text + "<br>\n");
            }else{
                String curContents = (String)sectionToContents.get(section);
                sectionToContents.put(section, curContents + text + "<br>\n");
            }
        }
        
        public void clear(String section){
            sectionToContents.remove(section);
        }
        
        public void addLink(HTMLPage page){
            append("Links", "<A HREF=" + page + ">" + page.getTitle() + "</A>");
        }
        
        public void addLinks(HTMLPage[] pages){
            for (int i = 0; i < pages.length; i++) {
                addLink(pages[i]);
            }
        }
        
        public String getTitle(){ return title; }
        
        public File write(){
            File output = new File(path, fileName);
            try {
                BufferedWriter writer = new BufferedWriter(new FileWriter(output));
                writer.write(constructPage());
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return output;
        }
        
        public String toString(){
            return fileName;
        }
        
        private String constructPage(){
            String header = "<html>\n<header><title>" + title + "</title></header>\n";
            String body = "<body>\n";
            body += constructContents();
            body += "</body>\n</html>";
            return header + body;
        }
        
        private String constructContents(){
            String contents = "";
            Iterator it = sectionToContents.keySet().iterator();
            while(it.hasNext()){
                String title = (String)it.next();
                String body = (String)sectionToContents.get(title);
                contents += "<b>" + title +":</b><br>\n";
                contents += body;
            }
            return contents;
        }
        
        private Map sectionToContents = new HashMap();
        
        private File path;
        
        private String fileName, title;
    }
}
