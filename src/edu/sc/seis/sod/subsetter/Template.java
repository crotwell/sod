/**
 * Template.java
 *
 * @author Created by Charles Groves
 */

package edu.sc.seis.sod.subsetter;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.apache.log4j.Logger;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public abstract class Template{
    public Template(Element el){
        setUp();
        if(el != null && el.getChildNodes() .getLength() > 0) parse(el.getChildNodes());
        else useDefaultConfig();
    }
    
    private void parse(NodeList nl){
        for (int i = 0; i < nl.getLength(); i++) {
            Node n = nl.item(i);
            if(n.getNodeType() == Node.TEXT_NODE) pieces.add(n.getNodeValue());
            else if(isInterpreted(n.getNodeName()))
                pieces.add(getInterpreter(n.getNodeName(), (Element)n));
            else{
                if(n.getChildNodes().getLength() == 0){
                    pieces.add("<" + n.getNodeName() + getAttrString(n) + "/>");
                }else{
                    pieces.add("<" + n.getNodeName()+ getAttrString(n) + ">");
                    parse(n.getChildNodes());
                    pieces.add("</" + n.getNodeName() + ">");
                }
            }
        }
    }
    
    protected void useDefaultConfig(){}
    
    protected void setUp(){}
    
    protected abstract Object getInterpreter(String tag, Element el);
    
    protected abstract boolean isInterpreted(String tag);
    
    private String getAttrString(Node n){
        String result = "";
        NamedNodeMap attr = n.getAttributes();
        for (int i = 0; i < attr.getLength(); i++) {
            result += " " + attr.item(i).getNodeName();
            result += "=\"" + attr.item(i).getNodeValue() + "\"";
        }
        return result;
    }
    
    public String getResults(){
        StringBuffer buf = new StringBuffer("");
        Iterator it = pieces.iterator();
        while(it.hasNext())  buf.append(it.next());
        return buf.toString();
    }
    
    public String toString(){ return getResults(); }
    
    private String template = "";
    
    protected List pieces = new ArrayList();
    
    private static Logger logger = Logger.getLogger(Template.class);
}
