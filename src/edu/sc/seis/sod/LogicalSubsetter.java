package edu.sc.seis.sod;

import java.util.LinkedList;
import java.util.List;
import org.apache.log4j.Logger;
import org.w3c.dom.Comment;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

/**
 * An abstract superclass for all of the logical operations, AND,
 * OR, NOT, XOR, for all of the types of subsetter. Manages the configuration
 * and storage of the subelements.
 *
 *
 * Created: Tue Mar 19 14:37:49 2002
 *
 * @author <a href="mailto:crotwell@pooh">Philip Crotwell</a>
 * @version
 */

public abstract class LogicalSubsetter implements Subsetter {
    /**
     * Creates a new <code>LogicalSubsetter</code> instance.
     *
     * @param config an <code>Element</code> value
     * @exception ConfigurationException if an error occurs
     */
    public LogicalSubsetter (Element config) throws ConfigurationException {
        processConfig(config);
    }

    /**
     * Describe <code>processConfig</code> method here.
     *
     * @param config an <code>Element</code> value
     * @exception ConfigurationException if an error occurs
     */
    protected void processConfig(Element config) throws ConfigurationException{
        NodeList children = config.getChildNodes();
        Node node;
        Class[] constructorArgTypes = new Class[1];
        constructorArgTypes[0] = Element.class;

        for (int i=0; i<children.getLength(); i++) {
            node = children.item(i);
            if (node instanceof Element) {
                Element subElement = (Element)node;
                if (subElement.getTagName().equals("description")) {
                    // skip description element
                    continue;
                }

                Object obj = SodUtil.load(subElement, getPackageName());
                if (obj instanceof Subsetter) {
                    filterList.add((Subsetter)obj);
                } else {
                    throw new ConfigurationException("obj not instance of Subsetter "+obj);
                } // end of else

            } else {
                if ( node instanceof Text && node.getNodeValue().trim().equals("")) {
                    // don't worry about empty text
                } else if ( node instanceof Comment) {
                    // don't worry about comments
                } else {
                    throw new ConfigurationException("node not instance of Element "+node);
                } // end of else
            } // end of else

        } // end of for (int i=0; i<children.getSize(); i++)

    }


    /**
     * Describe <code>getPackageName</code> method here.
     *
     * @return a <code>String</code> value
     */
    public abstract String getPackageName();

    /**
     * Describe variable <code>filterList</code> here.
     *
     */
    protected List filterList = new LinkedList();

    private static Logger logger =
        Logger.getLogger(LogicalSubsetter.class);

}// LogicalSubsetter
