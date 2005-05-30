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
import javax.swing.JLabel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.xml.transform.TransformerException;
import org.apache.log4j.Logger;
import org.apache.xpath.XPathAPI;
import org.w3c.dom.Attr;
import org.w3c.dom.DOMException;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import edu.iris.Fissures.model.UnitImpl;

public class EditorUtil {

    public static Box getLabeledTextField(Element element)
            throws TransformerException {
        return labelTextField(element.getTagName(),
                              getTextField((Text)XPathAPI.selectSingleNode(element,
                                                                           "text()")));
    }

    public static Box labelTextField(String name, JTextField jtf) {
        Box b = Box.createHorizontalBox();
        b.add(getLabel(name));
        b.add(jtf);
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

    public static JComponent getLabel(String text) {
        return new JLabel(SimpleGUIEditor.getDisplayName(text) + ":");
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
        if(!Character.isUpperCase(c)) {
            return ("" + c).toUpperCase() + in.substring(1);
        }
        return in;
    }

    /**
     * creates a JPanel with the bottom component slightly indented relative to
     * the bottome one.
     */
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

    public static JComponent getLabeledComboBox(Element element, Object[] vals)
            throws TransformerException {
        return getLabeledComboBox(element, getComboBox(element, vals));
    }

    public static JComponent getLabeledComboBox(Element element,
                                                Object[] vals,
                                                Object selected)
            throws TransformerException {
        return getLabeledComboBox(element, getComboBox(element, vals, selected));
    }

    public static JComponent getLabeledComboBox(Element element,
                                                JComboBox comboBox)
            throws TransformerException {
        Box b = Box.createHorizontalBox();
        b.add(getLabel(SimpleGUIEditor.getDisplayName(element.getTagName())));
        b.add(comboBox);
        b.add(Box.createHorizontalGlue());
        return b;
    }

    public static JComboBox getComboBox(Element element, Object[] vals)
            throws TransformerException {
        Node node = XPathAPI.selectSingleNode(element, "text()");
        if(node != null) {
            Text text = (Text)node;
            return getComboBox(element, vals, text.getNodeValue());
        } else {
            logger.warn("No text node inside node " + element.getTagName());
            return getComboBox(element, vals, "");
        }
    }

    public static JComboBox getComboBox(Element element,
                                        Object[] vals,
                                        Object selected)
            throws TransformerException {
        Node node = XPathAPI.selectSingleNode(element, "text()");
        Text text = (Text)node;
        if(text == null) {
            logger.debug("text node was null.  appending text node for selected object");
            text = element.getOwnerDocument()
                    .createTextNode(selected.toString());
            element.appendChild(text);
        }
        JComboBox combo = new JComboBox(vals);
        boolean found = false;
        for(int i = 0; i < vals.length; i++) {
            if(vals[i].equals(selected)) {
                found = true;
                break;
            }
        }
        if(!found) {
            combo.addItem(selected);
        }
        combo.setSelectedItem(selected);
        combo.addItemListener(new TextItemListener(text));
        return combo;
    }

    public static JSpinner createNumberSpinner(Text el,
                                               double min,
                                               double max,
                                               double step)
            throws TransformerException {
        return createNumberSpinner(el,
                                   new Double(min),
                                   new Double(max),
                                   new Double(step));
    }

    /**
     * Creates a number spinner with the initial value the Double.parseDouble of
     * the text in the Text node. The min, max and step are also given.
     */
    public static JSpinner createNumberSpinner(final Text text,
                                               Integer min,
                                               Integer max,
                                               Integer step) {
        try {
            final JSpinner spin = new JSpinner(new SpinnerNumberModel(new Integer(text.getNodeValue()),
                                                                      min,
                                                                      max,
                                                                      step));
            spin.addChangeListener(new ChangeListener() {

                public void stateChanged(ChangeEvent e) {
                    text.setNodeValue(spin.getValue().toString());
                }
            });
            return spin;
        } catch(RuntimeException e) {
            logger.warn(text.getNodeValue() + " " + min + " " + max, e);
            throw e;
        }
    }

    /**
     * Creates a number spinner with the initial value the Double.parseDouble of
     * the text in the Text node. The min, max and step are also given.
     */
    public static JSpinner createNumberSpinner(final Text text,
                                               Double min,
                                               Double max,
                                               Double step) {
        try {
            final JSpinner spin = new JSpinner(new SpinnerNumberModel(new Double(text.getNodeValue()),
                                                                      min,
                                                                      max,
                                                                      step));
            spin.addChangeListener(new ChangeListener() {

                public void stateChanged(ChangeEvent e) {
                    text.setNodeValue(spin.getValue().toString());
                }
            });
            return spin;
        } catch(RuntimeException e) {
            logger.warn(text.getNodeValue() + " " + min + " " + max, e);
            throw e;
        }
    }

    public static JComponent makeTimeIntervalTwiddler(Element el)
            throws TransformerException {
        return makeTimeIntervalTwiddler(el,
                                        new Integer(1),
                                        new Integer(Integer.MAX_VALUE));
    }

    public static JComponent makeTimeIntervalTwiddler(Element el,
                                                      Integer min,
                                                      Integer max)
            throws TransformerException {
        Box b = Box.createHorizontalBox();
        Text t = (Text)XPathAPI.selectSingleNode(el, "value/text()");
        b.add(EditorUtil.createNumberSpinner(t, min, max, new Integer(1)));
        Element e = (Element)XPathAPI.selectSingleNode(el, "unit");
        b.add(EditorUtil.getComboBox(e, SodGUIEditor.TIME_UNITS));
        b.add(Box.createHorizontalGlue());
        return b;
    }

    public static JComponent getBoxWithLabel(Element el) {
        Box b = Box.createHorizontalBox();
        b.add(Box.createHorizontalStrut(10));
        b.add(new JLabel(SimpleGUIEditor.getDisplayName(el.getTagName())));
        b.add(Box.createHorizontalGlue());
        return b;
    }

    public static JComponent makeQuantityTwiddler(Element element,
                                                  UnitImpl[] units)
            throws DOMException, TransformerException, NoSuchFieldException {
        return makeQuantityTwiddler(element, units, 0, 10000, 5);
    }

    public static JComponent makeQuantityTwiddler(Element element,
                                                  UnitImpl[] units,
                                                  int min,
                                                  int max,
                                                  int step)
            throws DOMException, TransformerException, NoSuchFieldException {
        Box b = Box.createHorizontalBox();
        NodeList kids = element.getChildNodes();
        String[] unitStrings = new String[units.length];
        for(int i = 0; i < units.length; i++) {
            unitStrings[i] = units[i].toString().toUpperCase();
        }
        for(int i = 0; i < kids.getLength(); i++) {
            if(kids.item(i) instanceof Element) {
                Element el = (Element)kids.item(i);
                Text text = (Text)XPathAPI.selectSingleNode(el, "text()");
                if(el.getTagName().equals("unit")) {
                    JComboBox unitCombo = getComboBox(el,
                                                      unitStrings,
                                                      text.getNodeValue());
                    b.add(unitCombo);
                    b.add(Box.createHorizontalStrut(10));
                } else if(el.getTagName().equals("value")) {
                    JSpinner valueSpinner = createNumberSpinner(text,
                                                                min,
                                                                max,
                                                                step);
                    b.add(valueSpinner);
                }
            }
        }
        return b;
    }

    private static Logger logger = Logger.getLogger(EditorUtil.class);
}