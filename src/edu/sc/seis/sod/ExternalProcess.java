package edu.sc.seis.sod;

import org.w3c.dom.*;

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
