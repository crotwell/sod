package edu.sc.seis.sod.status.eventArm;


import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.sc.seis.sod.SodUtil;
import edu.sc.seis.sod.Start;
import edu.sc.seis.sod.Status;
import edu.sc.seis.sod.status.FileWritingTemplate;
import edu.sc.seis.sod.status.GenericTemplate;
import edu.sc.seis.sod.status.TemplateFileLoader;
import edu.sc.seis.sod.status.eventArm.EventArmMonitor;
import java.io.IOException;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.log4j.Logger;
import org.w3c.dom.DOMException;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

public class EventStatusTemplate extends FileWritingTemplate implements EventArmMonitor{

    public EventStatusTemplate(Element el)throws IOException, SAXException, ParserConfigurationException {
        super(extractConstructorBaseDirArg(el), extractConstructorFilenameArg(el));
        Element config = TemplateFileLoader.getTemplate(SodUtil.getElement(el, "eventConfig"));
        config.removeChild(SodUtil.getElement(config, "filename"));
        parse(config);
    }

    private static String extractConstructorFilenameArg(Element el) throws IOException, SAXException, ParserConfigurationException, DOMException{
        String fileDir = extractConstructorBaseDirArg(el);
        Element eventConfigEl = SodUtil.getElement(el, "eventConfig");
        Element templateConfig = TemplateFileLoader.getTemplate(eventConfigEl);
        Element fileNameElement = SodUtil.getElement(templateConfig, "filename");
        String filename = fileNameElement.getFirstChild().getNodeValue();

        return filename;
    }

    private static String extractConstructorBaseDirArg(Element el){
        String fileDir = null;
        try{
            fileDir = SodUtil.getElement(el, "fileDir").getFirstChild().getNodeValue();
        }
        catch(NullPointerException e){
            Logger.getLogger(EventStatusTemplate.class).debug("fileDir element is null! using default");
        }
        if (fileDir == null){
            fileDir = FileWritingTemplate.getBaseDirectoryName();
        }
        return fileDir;
    }

    public void setArmStatus(String status) throws IOException {
        this.status = status;
        write();
    }

    public Object getTemplate(String tag, Element el){
        if(tag.equals("events")){
            return  new EventGroupTemplate(el);
        }else if(tag.equals("status")) return new StatusFormatter();
        else if (tag.equals("mapEventStatus")){
            return new MapImgSrc(el);
        }
        return super.getTemplate(tag, el);
    }



    private class MapImgSrc extends MapEventStatus implements GenericTemplate{

        public MapImgSrc(Element el){
            super(el);
        }

        public String getResult(){
            return SodUtil.getRelativePath(getOutputDirectory().toString() + '/' + getFilename(), fileLoc, "/");
        }
    }

    public void change(EventAccessOperations event, Status status) {
        write();
    }

    private class StatusFormatter implements GenericTemplate{
        public String getResult(){ return status; }
    }

    private Logger logger = Logger.getLogger(EventStatusTemplate.class);
    private String status = "";
}
