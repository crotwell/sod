/**
 * WaveformStatusTemplate.java
 *
 * @author Created by Charles Groves
 */

package edu.sc.seis.sod.status.waveformArm;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.log4j.Logger;
import org.w3c.dom.DOMException;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.EventChannelPair;
import edu.sc.seis.sod.SodUtil;
import edu.sc.seis.sod.status.FileWritingTemplate;
import edu.sc.seis.sod.status.TemplateFileLoader;
import edu.sc.seis.sod.status.eventArm.EventStatusTemplate;

public class WaveformStatusTemplate extends FileWritingTemplate implements WaveformMonitor{
    public WaveformStatusTemplate(Element el)throws IOException, SAXException, ParserConfigurationException, ConfigurationException{
        super(extractConstructorBaseDirArg(el), extractConstructorFilenameArg(el));
        Element config = TemplateFileLoader.getTemplate(SodUtil.getElement(el, "config"));
        config.removeChild(SodUtil.getElement(config, "filename"));
        parse(config);
    }

    private static String extractConstructorFilenameArg(Element el) throws IOException, SAXException, ParserConfigurationException, DOMException{
        String fileDir = extractConstructorBaseDirArg(el);
        Element eventConfigEl = SodUtil.getElement(el, "config");
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

    protected Object getTemplate(String tag, Element el) throws ConfigurationException {
        if(tag.equals("events")){
            WaveformEventGroup ect = new WaveformEventGroup(el);
            eventTemplates.add(ect);
            return ect;
        }
        return super.getTemplate(tag, el);
    }

    public void update(EventChannelPair ecp) {
        Iterator it = eventTemplates.iterator();
        while(it.hasNext()) ((WaveformMonitor)it.next()).update(ecp);
        write();
    }

    private List eventTemplates = new ArrayList();
}
