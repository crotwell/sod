package edu.sc.seis.sod.status.eventArm;


import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.sc.seis.sod.EventStatus;
import edu.sc.seis.sod.SodUtil;
import edu.sc.seis.sod.database.event.EventCondition;
import edu.sc.seis.sod.status.FileWritingTemplate;
import edu.sc.seis.sod.status.GenericTemplate;
import edu.sc.seis.sod.status.NowTemplate;
import edu.sc.seis.sod.status.TemplateFileLoader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.DOMException;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

public class EventStatusTemplate extends FileWritingTemplate implements EventStatus{
    
    public EventStatusTemplate(Element el)throws IOException, SAXException, ParserConfigurationException {
        super(extractConstructorArg(el));
        Element config = TemplateFileLoader.getTemplate(SodUtil.getElement(el, "eventConfig"));
        config.removeChild(SodUtil.getElement(config, "filename"));
        parse(config);
    }
    
    private static String extractConstructorArg(Element el) throws IOException, SAXException, ParserConfigurationException, DOMException{
        String fileDir = SodUtil.getElement(el, "fileDir").getFirstChild().getNodeValue();
        Element eventConfigEl = SodUtil.getElement(el, "eventConfig");
        Element templateConfig = TemplateFileLoader.getTemplate(eventConfigEl);
        Element fileNameElement = SodUtil.getElement(templateConfig, "filename");
        String filename = fileNameElement.getFirstChild().getNodeValue();
        
        return fileDir + '/' + filename;
    }
    
    public void setArmStatus(String status) throws IOException {
        this.status = status;
        write();
    }
    
    public Object getTemplate(String tag, Element el){
        if(tag.equals("events")){
            EventGroupTemplate egt = new EventGroupTemplate(el);
            internalStatusWatchers.add(egt);
            return egt;
        }else if(tag.equals("status")) return new StatusFormatter();
        else if (tag.equals("mapEventStatus")){
            MapEventStatus mapStatus = new MapImgSrc(el);
            if (!mapStatus.isDuplicateForLocation()){
                internalStatusWatchers.add(mapStatus);
            }
            return mapStatus;
        }
        return null;
    }
    

    
    private class MapImgSrc extends MapEventStatus implements GenericTemplate{
        
        public MapImgSrc(Element el){
            super(el);
        }
        
        public String getResult(){
            return SodUtil.getRelativePath(getOutputDirectory().toString() + '/' + getFilename(), fileLoc, "/");
        }
    }
    
    public void change(EventAccessOperations event, EventCondition status) throws Exception {
        Iterator it = internalStatusWatchers.iterator();
        while(it.hasNext()) ((EventStatus)it.next()).change(event, status);
        write();
    }
    
    private class StatusFormatter implements GenericTemplate{
        public String getResult(){ return status; }
    }
    
    private List internalStatusWatchers = new ArrayList();
    
    private String status = "";
}
