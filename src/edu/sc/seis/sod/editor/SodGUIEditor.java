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
import javax.swing.JLabel;
import org.w3c.dom.*;
import org.apache.xpath.XPathAPI;
import javax.swing.SwingConstants;



public class SodGUIEditor extends SimpleGUIEditor {

    SodGUIEditor(String[] args) throws IOException, ParserConfigurationException, TransformerException, DOMException, SAXException {
        super(args);
        frameName = "SOD Editor";
    }


    void addElementToPanel(JPanel panel, Element element, GridBagConstraints gbc) {
        try {
            if (element.getTagName().equals("property")) {
                PropertyEditor edit = new PropertyEditor();
                GridBagConstraints clone = (GridBagConstraints)gbc.clone();
                clone.fill = clone.HORIZONTAL;
                panel.add(edit.getGUI(element), gbc);
                gbc.gridy++;
            } else {
                super.addElementToPanel(panel, element, gbc);
            }
        } catch (Exception e) {
            super.addElementToPanel(panel, element, gbc);
        }
    }

    public static void main(String[] args) throws IOException, ParserConfigurationException, TransformerException, DOMException, SAXException {
        BasicConfigurator.configure();
        SodGUIEditor gui = new SodGUIEditor(args);
        gui.start();
        System.out.println("Done editing.");
    }
}

