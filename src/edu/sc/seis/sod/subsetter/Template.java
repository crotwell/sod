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
    
    /** A subclass should use this method to add some templates to the templates
     *list so it returns decent results
     */
    protected void useDefaultConfig(){}
    
    /** This method is called in Template's constructor so that if subclasses
     * need to initialize instance variables for use in getTemplate, they can.
     * Otherwise, as Template's constructor must be called before the subclasses
     * constructor, and Template's constructor calls parse which may need
     * instances in the subclass
     */
    protected void setUp(){}
    
    /**
     *returns an object of the template type that this class uses, and returns
     * the passed in text when the getResult method of that template type is
     * called
     */
    protected abstract Object textTemplate(String text);
    
    /**if this class has an template for this tag, it creates it using the
     * passed in element and returns it.  Otherwise it returns null.
     */
    protected abstract Object getTemplate(String tag, Element el);
    
    private void parse(NodeList nl){
        for (int i = 0; i < nl.getLength(); i++) {
            Node n = nl.item(i);
            if(n.getNodeType() == Node.TEXT_NODE)
                templates.add(textTemplate(n.getNodeValue()));
            else{
                String name = n.getNodeName();
                Object template = getTemplate(name, (Element)n);
                if(template != null) templates.add(template);
                else{
                    templates.add(textTemplate("<" + name));
                    addAttributes(n);
                    if(n.getChildNodes().getLength() == 0){
                        templates.add(textTemplate("/>"));
                    }else{
                        templates.add(textTemplate(">"));
                        parse(n.getChildNodes());
                        templates.add(textTemplate("</" + name + ">"));
                    }
                }
            }
        }
    }
    
    private int addAttributes(Node n){
        templates.add(textTemplate(getAttrString(n)));
        int numChild = n.getChildNodes().getLength();
        int attrCount = 0;
        for (int i = 0; i < numChild && childName(i, n).equals("attribute"); i++) {
            Element attr = (Element)n.getChildNodes().item(i);
            templates.add(textTemplate(" " + attr.getAttribute("name") + "=\""));
            parse(attr.getChildNodes());
            templates.add(textTemplate("\""));
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
    
    protected List templates = new ArrayList();
    
    private static Logger logger = Logger.getLogger(Template.class);
}
