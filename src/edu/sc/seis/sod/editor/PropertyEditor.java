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

    /**
     * Method getGUI
     *
     * @param    element             an Element
     *
     * @return   a JComponent
     *
     */
    public JComponent getGUI(Element element) throws TransformerException {
        JPanel panel = new JPanel();
        if (element.getTagName().equals("property")) {
            panel.setLayout(new GridBagLayout());
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.gridx=0;
            gbc.gridy=0;
            gbc.fill = gbc.BOTH;
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
            addTextField(panel, text, gbc);
            gbc.gridx--;
        }
        return panel;
    }

    void addTextField(JPanel panel, Text text, GridBagConstraints gbc) {
        JTextField textField = new JTextField();
        textField.setText(text.getNodeValue().trim());
        TextListener textListen = new TextListener(text);
        textField.getDocument().addDocumentListener(textListen);
        panel.add(textField, gbc);
    }

}

