package edu.sc.seis.sod.process;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.process.Process;
import edu.sc.seis.sod.SodElement;
import edu.sc.seis.sod.SodUtil;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * ExternalProcess.java
 *
 *
 * Created: Fri Apr 12 12:03:11 2002
 *
 * @author <a href="mailto:">Philip Crotwell</a>
 * @version
 */

public abstract class ExternalProcess implements Process {
    public ExternalProcess (Element config) throws ConfigurationException {
    NodeList children = config.getChildNodes();
    Node node;
    for (int i=0; i < children.getLength(); i++) {
        node = children.item(i);
        if (node instanceof Element) {
        Element subElement = (Element)node;
        String tagName = subElement.getTagName();
        if (tagName.equals("classname")) {
            classname = SodUtil.getText(subElement);
        }
        }
    }
    SodElement se = (SodElement)SodUtil.loadExternal(config);
    process = (Process)se;
    
    }
    
    public abstract void invoke();
    
    Process process;

    String classname;
    
}// ExternalProcess
