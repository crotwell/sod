/**
 * ExternalFileTemplate.java
 *
 * @author Created by Charles Groves
 */

package edu.sc.seis.sod.subsetter;

import edu.sc.seis.sod.Start;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import org.apache.log4j.Logger;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import javax.xml.parsers.ParserConfigurationException;

public abstract class ExternalFileTemplate extends Template{
    public ExternalFileTemplate(Element el)throws IOException{
        super(getTemplate(el));
        outputLocation = getOutLoc(el);
    }
    
    private static Element getTemplate(Element el) throws MalformedURLException,
        IOException{
        Attr attr =  (Attr)el.getAttributes().getNamedItem("xlink:link");
        URL loc = null;
        if(attr.getValue().startsWith("jar:")){
            loc = el.getClass().getClassLoader().getResource(attr.getValue().substring(4));
        }else loc = new URL(attr.getValue());
        try {
            Document doc = Start.createDoc(new InputSource(loc.openStream()));
            return (Element)doc.getFirstChild();
        } catch (SAXException e){   throw new RuntimeException(e);
        }catch (ParserConfigurationException e) {throw new RuntimeException(e);}
    }
    
    private static String getOutLoc(Element el){
        Attr attr = (Attr)el.getAttributes().getNamedItem("outputLocation");
        File outFile = new File(attr.getValue());
        try {
            outFile.getCanonicalFile().getParentFile().mkdirs();
        } catch (IOException e) {}
        return attr.getValue();
    }
    
    public void update(){
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(new File(outputLocation)));
            writer.write(getResults());
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private String outputLocation;
    
    private static Logger logger = Logger.getLogger(ExternalFileTemplate.class);
}
