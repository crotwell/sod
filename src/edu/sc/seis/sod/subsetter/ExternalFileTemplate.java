/**
 * ExternalFileTemplate.java
 *
 * @author Created by Charles Groves
 */

package edu.sc.seis.sod.subsetter;

import edu.sc.seis.sod.CommonAccess;
import edu.sc.seis.sod.Start;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.log4j.Logger;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public abstract class ExternalFileTemplate extends Template implements GenericTemplate{
    /**This constructor expects and element of the form
     * <templateTagName xlink:link="template configuration file" outputLocation="file output location"/>
     * It simple extracts the main element contained in template configuration file
     * and uses the value of the outputLocation attribute to call the
     * ExternalFileTemplate(Element, String) constructor
     */
    public ExternalFileTemplate(Element el)throws IOException{
        this(getTemplate(el), el.getAttribute("outputLocation"));
    }
    
    public ExternalFileTemplate(Element el, String loc){
        super(el);
        this.outputLocation = testOutputLoc(loc);
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
    
    private static String testOutputLoc(String loc){
        File outFile = new File(loc);
        try {
            outFile.getCanonicalFile().getParentFile().mkdirs();
        } catch (IOException e) {
            CommonAccess.getCommonAccess().handleException(e, "Trouble making directories for output location for an external file template");
        }
        return loc;
    }
    
    public void update(){
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(new File(outputLocation)));
            writer.write(getResult());
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public String getResult(){
        StringBuffer buf = new StringBuffer();
        Iterator e = templates.iterator();
        while(e.hasNext()) buf.append(((GenericTemplate)e.next()).getResult());
        return buf.toString();
    }
    
    protected Object textTemplate(final String text){
        return new GenericTemplate(){
            public String getResult() { return text; }
        };
    }
    
    private String outputLocation;
    
    private static Logger logger = Logger.getLogger(ExternalFileTemplate.class);
}
