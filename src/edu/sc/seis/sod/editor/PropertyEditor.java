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
import org.apache.xpath.XPathAPI;
import javax.xml.transform.TransformerException;



public class PropertyEditor implements EditorPlugin {

    public JComponent getGUI(Element element) throws TransformerException {
        JPanel panel = new JPanel();
        if (element.getTagName().equals("property")) {
            panel.setLayout(new GridBagLayout());
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.gridx=0;
            gbc.gridy=0;
            gbc.fill = gbc.HORIZONTAL;
            gbc.weightx = 1;
            gbc.weighty = 1;
            Node node = XPathAPI.selectSingleNode(element, "name/text()");
            Text text = (Text)node;
            JLabel label = new JLabel(text.getNodeValue()+" = ");
            label.setHorizontalTextPosition(SwingConstants.RIGHT);
            panel.add(label, gbc);
            gbc.gridx++;
            node = XPathAPI.selectSingleNode(element, "value/text()");
            text = (Text)node;
            panel.add(EditorUtil.getTextField(text), gbc);
            gbc.gridx--;
        }
        return panel;
    }

}

