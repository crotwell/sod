package edu.sc.seis.sod;

import org.w3c.dom.*;

/**
 * AbstractSource.java
 *
 *
 * Created: Wed Mar 20 13:52:55 2002
 *
 * @author <a href="mailto:">Srinivasa Telukutla</a>
 * @version
 */

public abstract class AbstractSource implements Source{
    /**
     * Creates a new <code>AbstractSource</code> instance.
     *
     * @param config an <code>Element</code> value
     */
    public AbstractSource (Element config){
	this.config = config;
    }

    /**
     * Describe <code>getDNSName</code> method here.
     *
     * @return a <code>String</code> value
     */
    public String getDNSName() {

	NodeList children = config.getChildNodes();
	Node node;
	for (int i=0; i<children.getLength(); i++) {
	    node = children.item(i);
	    if (node instanceof Element) {
		String tagName  = ((Element)node).getTagName();
		if(tagName.equals("dns")) {

		    return SodUtil.getText((Element)node);

		}
	    } // end of if (node instanceof Element)
	} // end of for (int i=0; i<children.getSize(); i++)
	return null;
    }

    /**
     * Describe <code>getSourceName</code> method here.
     *
     * @return a <code>String</code> value
     */
    public String getSourceName() {
		NodeList children = config.getChildNodes();
	Node node;
	for (int i=0; i<children.getLength(); i++) {
	    node = children.item(i);
	    if (node instanceof Element) {
		String tagName  = ((Element)node).getTagName();
		if(tagName.equals("name")) {

		    return SodUtil.getText((Element)node);

		}
	    } // end of if (node instanceof Element)
	} // end of for (int i=0; i<children.getSize(); i++)
	return null;

    }
  
    private Element config;
   
}// AbstractSource
