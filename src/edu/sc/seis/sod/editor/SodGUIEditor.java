/**
 * SodGUIEditor.java
 *
 * @author Created by Omnicore CodeGuide
 */

package edu.sc.seis.sod.editor;

import java.awt.GridBagConstraints;
import java.io.IOException;
import javax.swing.JPanel;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import org.apache.log4j.BasicConfigurator;
import org.w3c.dom.DOMException;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;



public class SodGUIEditor extends SimpleGUI {

    SodGUIEditor(String[] args) throws IOException, ParserConfigurationException, TransformerException, DOMException, SAXException {
        super(args);
    }


    void addElementToPanel(JPanel panel, Element element, GridBagConstraints gbc) {
        super.addElementToPanel(panel, element, gbc);
    }

    public static void main(String[] args) throws IOException, ParserConfigurationException, TransformerException, DOMException, SAXException {
        BasicConfigurator.configure();
        SodGUIEditor gui = new SodGUIEditor(args);
        gui.start();
        System.out.println("Done editing.");
    }
}

