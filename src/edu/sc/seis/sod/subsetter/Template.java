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
            if(n.getNodeType() == Node.TEXT_NODE) pieces.add(textTemplate(n.getNodeValue()));
            else if(isInterpreted(n.getNodeName()))
                pieces.add(getInterpreter(n.getNodeName(), (Element)n));
            else{
                pieces.add(textTemplate("<" + n.getNodeName()));
                addAttributes(n);
                if(n.getChildNodes().getLength() == 0){
                    pieces.add(textTemplate("/>"));
                }else{
                    pieces.add(textTemplate(">"));
                    parse(n.getChildNodes());
                    pieces.add(textTemplate("</" + n.getNodeName() + ">"));
                }
            }
        }
    }
    
    protected void useDefaultConfig(){}
    
    protected void setUp(){}
    
    protected abstract Object textTemplate(String text);
    
    protected abstract Object getInterpreter(String tag, Element el);
    
    protected abstract boolean isInterpreted(String tag);
    
    private int addAttributes(Node n){
        pieces.add(textTemplate(getAttrString(n)));
        int numChild = n.getChildNodes().getLength();
        int attrCount = 0;
        for (int i = 0; i < numChild && childName(i, n).equals("attribute"); i++) {
            Element attr = (Element)n.getChildNodes().item(i);
            pieces.add(textTemplate(" " + attr.getAttribute("name") + "=\""));
            parse(attr.getChildNodes());
            pieces.add(textTemplate("\""));
            n.removeChild(attr);
        }
        return attrCount;
    }
    
    private static String childName(int i, Node n){
        return n.getChildNodes().item(i).getNodeName();
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
    
    private String template = "";
    
    protected List pieces = new ArrayList();
    
    private static Logger logger = Logger.getLogger(Template.class);
}

