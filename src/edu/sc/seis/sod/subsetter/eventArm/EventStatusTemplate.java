package edu.sc.seis.sod.subsetter.eventArm;

import org.w3c.dom.*;

import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.sc.seis.sod.EventStatus;
import edu.sc.seis.sod.RunStatus;
import edu.sc.seis.sod.Start;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.apache.log4j.Logger;
import org.xml.sax.InputSource;

public class EventStatusTemplate implements EventStatus{
    public EventStatusTemplate(Element el)throws IOException{
        URL templateFile = getTemplate(el);
        outputLocation = getOutLoc(el);
        try {
            Document doc = Start.createDoc(new InputSource(templateFile.openStream()));
            Element template = (Element)doc.getFirstChild();
            parse(template.getChildNodes());
        } catch (Exception e) {
            logger.error("Problem creating EventStatusTemplate");
            pieces.clear();
            pieces.add(e);
            e.printStackTrace();
        }
    }
    
    private void parse(NodeList nl){
        for (int i = 0; i < nl.getLength(); i++) {
            Node n = nl.item(i);
            if(n.getNodeType() == Node.TEXT_NODE) pieces.add(n.getNodeValue());
            else if(n.getNodeName().equals("events"))
                pieces.add(new EventGroupTemplate((Element)n));
            else if(n.getNodeName().equals("status"))
                pieces.add(new StatusFormatter());
            else{
                if(n.getChildNodes().getLength() == 0) pieces.add("<" + n.getNodeName() + getAttrString(n) + "/>");
                else{
                    pieces.add("<" + n.getNodeName()+ getAttrString(n) + ">");
                    parse(n.getChildNodes());
                    pieces.add("</" + n.getNodeName() + ">");
                }
            }
        }
    }
    
    private String getAttrString(Node n){
        String result = "";
        NamedNodeMap attr = n.getAttributes();
        for (int i = 0; i < attr.getLength(); i++) {
            result += " " + attr.item(i).getNodeName();
            result += "=\"" + attr.item(i).getNodeValue() + "\"";
        }
        return result;
    }
    
    private static URL getTemplate(Element el) throws MalformedURLException{
        Attr attr =  (Attr)el.getAttributes().getNamedItem("xlink:link");
        if(attr.getValue().startsWith("jar:")){
            return el.getClass().getClassLoader().getResource(attr.getValue().substring(4));
        }
        return new URL(attr.getValue());
    }
    
    private static String getOutLoc(Element el){
        Attr attr = (Attr)el.getAttributes().getNamedItem("outputLocation");
        File outFile = new File(attr.getValue());
        try {
            outFile.getCanonicalFile().getParentFile().mkdirs();
        } catch (IOException e) {}
        return attr.getValue();
    }
    
    public void setArmStatus(String status) {
        this.status = status;
        update();
    }
    
    public void change(EventAccessOperations event, RunStatus status) {
        Iterator it = pieces.iterator();
        while(it.hasNext()){
            Object cur = it.next();
            if(cur instanceof EventGroupTemplate){
                ((EventGroupTemplate)cur).change(event, status);
            }
        }
        update();
    }
    
    public void update(){
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(new File(outputLocation)));
            writer.write(toString());
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public String toString(){
        StringBuffer buf = new StringBuffer("");
        Iterator it = pieces.iterator();
        while(it.hasNext()){
            buf.append(it.next());
        }
        return buf.toString();
    }
    
    private class StatusFormatter{
        public String toString(){ return status; }
    }
    
    private String outputLocation;
    
    private String template = "";
    
    private String status = "";
    
    private List pieces = new ArrayList();
    
    private static Logger logger = Logger.getLogger(EventStatusTemplate.class);
}
