/**
 * EditorUtil.java
 *
 * @author Created by Omnicore CodeGuide
 */

package edu.sc.seis.sod.editor;

import java.awt.Dimension;
import javax.swing.Box;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JTextField;
import javax.xml.transform.TransformerException;
import org.apache.xpath.XPathAPI;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.Text;

public class EditorUtil {


    public static JTextField getTextField(Text text) {
        JTextField textField = new JTextField();
        textField.setText(text.getNodeValue().trim());
        TextListener textListen = new TextListener(text);
        textField.getDocument().addDocumentListener(textListen);
        return textField;
    }

    public static String capFirstLetter(String in) {
        char c = in.charAt(0);
        if ( ! Character.isUpperCase(c)) {
            return (""+c).toUpperCase()+in.substring(1);
        }
        return in;
    }


    /** creates a JPanel with the bottom component slightly indented relative
     to the bottome one. */
    public static Box indent(JComponent top, JComponent bottom) {
        Box box = Box.createVerticalBox();
        Box topRow = Box.createHorizontalBox();
        box.add(topRow);
        Box botRow = Box.createHorizontalBox();
        box.add(botRow);

        topRow.add(top);
        topRow.add(Box.createGlue());
        botRow.add(Box.createRigidArea(new Dimension(10, 10)));
        botRow.add(bottom);
        botRow.add(Box.createGlue());
        return box;
    }


    public static JComboBox getComboBox(Element element, Object[] vals) throws TransformerException {
        Node node = XPathAPI.selectSingleNode(element, "text()");
        Text text = (Text)node;
        JComboBox combo = new JComboBox(vals);
        combo.addItem(text.getNodeValue());
        combo.setSelectedItem(text.getNodeValue());
        combo.addItemListener(new TextItemListener(text));
        return combo;
    }

}

