/**
 * EditorUtil.java
 *
 * @author Created by Omnicore CodeGuide
 */

package edu.sc.seis.sod.editor;

import javax.swing.*;

import java.awt.Dimension;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.xml.transform.TransformerException;
import org.apache.log4j.Logger;
import org.apache.xpath.XPathAPI;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.Text;

public class EditorUtil {

    public static Box getLabeledTextField(Element element) throws TransformerException {
        Box b = Box.createHorizontalBox();
        b.add(getLabel(element.getTagName()));
        b.add(getTextField((Text)XPathAPI.selectSingleNode(element, "text()")));
        return b;
    }

    public static Box getLabeledTextField(Attr attr) {
        Box b = Box.createHorizontalBox();
        b.add(getLabel(attr.getName()));
        JTextField textField = new JTextField();
        textField.setText(attr.getNodeValue());
        TextListener textListen = new TextListener(attr);
        textField.getDocument().addDocumentListener(textListen);
        b.add(textField);
        return b;
    }

    public static JLabel getLabel(String text) {
        return new JLabel(SimpleGUIEditor.getDisplayName(text)+":");
    }

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
        box.add(Box.createRigidArea(new Dimension(10, 10)));
        box.add(topRow);
        Box botRow = Box.createHorizontalBox();
        box.add(botRow);

        topRow.add(top);
        topRow.add(Box.createGlue());
        botRow.add(Box.createRigidArea(new Dimension(10, 1)));
        botRow.add(bottom);
        botRow.add(Box.createGlue());
        return box;
    }


    public static JComboBox getComboBox(Element element, Object[] vals) throws TransformerException {
        Node node = XPathAPI.selectSingleNode(element, "text()");
        if (node != null) {
            Text text = (Text)node;
            return getComboBox(element, vals, text.getNodeValue());
        } else {
            logger.warn("No text node inside node "+element.getTagName());
            return getComboBox(element, vals, "");
        }
    }

    public static JComboBox getComboBox(Element element, Object[] vals, Object selected) throws TransformerException {
        Node node = XPathAPI.selectSingleNode(element, "text()");
        Text text = (Text)node;
        JComboBox combo = new JComboBox(vals);
        boolean found = false;

        for (int i = 0; i < vals.length; i++) {
            if (vals[i].equals(selected)) {
                found = true;
                break;
            }
        }
        if ( ! found) {
            combo.addItem(selected);
        }
        combo.setSelectedItem(selected);
        combo.addItemListener(new TextItemListener(text));
        return combo;
    }

    /** Creates a number spinner with the initial value the Double.parseDouble
     * of the text in the Text node. The min, max and step are also given. */
    public static JSpinner createNumberSpinner(final Text text,
                                               double min,
                                               double max,
                                               double step){
        final JSpinner spin = new JSpinner(new SpinnerNumberModel(Double.parseDouble(text.getNodeValue()),
                                                                  min, max, step));
        spin.addChangeListener(new ChangeListener(){
                    public void stateChanged(ChangeEvent e) {
                        text.setNodeValue(spin.getValue().toString());
                    }
                });
        return spin;
    }

    private static Logger logger = Logger.getLogger(EditorUtil.class);

}

