/*
 * Created on Jul 29, 2004
 */
package edu.sc.seis.sod.editor;

import javax.swing.Box;
import javax.swing.JComponent;
import javax.swing.border.TitledBorder;
import javax.xml.transform.TransformerException;
import org.apache.xpath.XPathAPI;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

/**
 * @author oliverpa
 */
public class BreqFastRequestSubsetterEditor implements EditorPlugin {

    public JComponent getGUI(Element element) throws TransformerException {
        Box box = Box.createVerticalBox();
        box.setBorder(new TitledBorder(SimpleGUIEditor.getDisplayName(element.getTagName())));
        box.add(EditorUtil.getLabeledTextField(extractElement(element,
                                                              "dataDirectory")));
        Box b = Box.createHorizontalBox();
        b.setBorder(new TitledBorder("Data Label"));
        b.add(Box.createHorizontalGlue());
        Box origBox = Box.createHorizontalBox();
        origBox.setBorder(new TitledBorder("Origin Format"));
        Text text2 = (Text)XPathAPI.selectSingleNode(element,
                                                     "label/originTime/text()");
        origBox.add(EditorUtil.getTextField(text2));
        b.add(origBox);
        Box innerBox = Box.createHorizontalBox();
        innerBox.setBorder(new TitledBorder("Suffix"));
        //having trouble getting this text node...doing funky stuff to make it
        // work
        NodeList nodes = XPathAPI.selectNodeList(element, "label/text()");
        text2 = (Text)nodes.item(nodes.getLength() - 1);
        innerBox.add(EditorUtil.getTextField(text2));
        b.add(innerBox);
        b.add(Box.createHorizontalGlue());
        box.add(b);
        box.add(EditorUtil.getLabeledTextField(extractElement(element, "name")));
        box.add(EditorUtil.getLabeledTextField(extractElement(element, "inst")));
        box.add(EditorUtil.getLabeledTextField(extractElement(element, "mail")));
        box.add(EditorUtil.getLabeledTextField(extractElement(element, "email")));
        box.add(EditorUtil.getLabeledTextField(extractElement(element, "phone")));
        box.add(EditorUtil.getLabeledTextField(extractElement(element, "fax")));
        box.add(EditorUtil.getLabeledComboBox(extractElement(element, "media"),
                                              MEDIA_CODES));
        box.add(EditorUtil.getLabeledComboBox(extractElement(element,
                                                             "altmedia1"),
                                              MEDIA_CODES));
        box.add(EditorUtil.getLabeledComboBox(extractElement(element,
                                                             "altmedia2"),
                                              MEDIA_CODES));
        box.add(EditorUtil.getLabeledComboBox(extractElement(element, "quality"),
                                              QUALITY_CODES));
        return box;
    }

    private static Element extractElement(Element element, String name)
            throws TransformerException {
        Node n = XPathAPI.selectSingleNode(element, name);
        return (Element)n;
    }

    public static final String[] QUALITY_CODES = {"b", "e", "q", "d", "r"};

    public static final String[] QUALITY_VALUES = {"Best Data",
                                                   "Everything",
                                                   "Quality Controlled",
                                                   "Intermediate Quality",
                                                   "Raw Data"};

    public static final String[] MEDIA_CODES = {"Electronic",
                                                "EXABYTE",
                                                "DAT",
                                                "DLT"};
}