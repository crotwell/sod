/**
 * PropertyEditor.java
 *
 * @author Created by Omnicore CodeGuide
 */

package edu.sc.seis.sod.editor;
import javax.swing.JComponent;
import org.w3c.dom.*;
import javax.swing.*;
import java.awt.*;
import java.util.*;
import org.apache.xpath.XPathAPI;
import javax.xml.transform.TransformerException;
import java.util.Properties;
import java.io.IOException;
import java.awt.event.*;

public class PropertyEditor implements EditorPlugin {

    public JComponent getGUI(Element element) throws TransformerException {
        JPanel panel = new JPanel(new GridBagLayout());
        int gridy = 0;
        NodeList nl = element.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            if(nl.item(i) instanceof Element){
                Element childEl = (Element)nl.item(i);
                addLabel(EditorUtil.getLabel(childEl.getTagName()), panel, gridy);
                addTwiddler(makeTwiddler(childEl), panel, gridy++);
                addSpacer(panel, gridy++, false);
            }
        }
        addSpacer(panel, gridy++, true);
        return panel;
    }

    private void addLabel(JComponent label, JComponent recipient, int gridy){
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = gridy;
        gbc.fill = GridBagConstraints.NONE;
        gbc.gridwidth = 1;
        gbc.gridheight = 1;
        gbc.anchor = GridBagConstraints.EAST;
        recipient.add(label, gbc);
    }

    private void addTwiddler(JComponent twiddler, JComponent recipient, int gridy){
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = gridy;
        gbc.fill = GridBagConstraints.NONE;
        gbc.gridwidth = 1;
        gbc.gridheight = 1;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.weightx = 1;
        Dimension minSize = twiddler.getMinimumSize();
        if(minSize.width < 300){ gbc.ipadx = (300 - minSize.width)/2; }
        recipient.add(twiddler, gbc);
    }

    private void addSpacer(JComponent recipient, int gridy, boolean finalSpacer){
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.weightx = 1;
        gbc.gridy = gridy;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridheight = 1;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        if(finalSpacer){ gbc.weighty = 1; }
        recipient.add(new JPanel(), gbc);
    }

    private JComponent makeTwiddler(Element el) throws TransformerException{
        for (int i = 0; i < timeQuantityEls.length; i++) {
            if(timeQuantityEls[i].equals(el.getTagName())){
                return makeTimeIntervalTwiddler(el);
            }
        }
        Text t = (Text)XPathAPI.selectSingleNode(el, "text()");
        return EditorUtil.getTextField(t);
    }

    private JComponent makeTimeIntervalTwiddler(Element el) throws TransformerException{
        Box b = Box.createHorizontalBox();
        Text t = (Text)XPathAPI.selectSingleNode(el, "value/text()");
        b.add(EditorUtil.createNumberSpinner(t, 1, 50, 1));
        Element e = (Element)XPathAPI.selectSingleNode(el, "unit");
        b.add(EditorUtil.getComboBox(e, SodGUIEditor.TIME_UNITS));
        b.add(Box.createHorizontalGlue());
        return b;
    }

    private String[] timeQuantityEls = { "eventLag", "eventQueryIncrement",
            "eventRefreshInterval", "maxRetryDelay" };
}


