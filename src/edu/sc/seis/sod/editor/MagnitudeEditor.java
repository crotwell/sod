/**
 * MagnitudeEditor.java
 *
 * @author Charles Groves
 */

package edu.sc.seis.sod.editor;
import javax.swing.*;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeListener;
import org.apache.xpath.XPathAPI;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.Text;
import javax.swing.event.ChangeEvent;



public class MagnitudeEditor implements EditorPlugin{
    public JComponent getGUI(Element element) throws Exception {
        Box b = Box.createHorizontalBox();
        b.setBorder(new TitledBorder(SimpleGUIEditor.getDisplayName(element.getTagName())));
        b.add(new JLabel(SimpleGUIEditor.getDisplayName("magType")));
        Node node = XPathAPI.selectSingleNode(element, "magType/text()");
        final Text text = (Text)node;
        final JComboBox magBox = new JComboBox(new String[]{"M", "%", "MB"});
        magBox.setSelectedItem(text.getNodeValue());
        b.add(magBox);
        b.add(Box.createHorizontalGlue());
        magBox.addActionListener(new ActionListener(){
                    public void actionPerformed(ActionEvent e) {
                        text.setNodeValue(magBox.getSelectedItem().toString());
                    }
                });
        b.add(new JLabel(SimpleGUIEditor.getDisplayName("min")));
        b.add(makeSpinner((Text)XPathAPI.selectSingleNode(element, "min/text()")));
        b.add(Box.createHorizontalGlue());
        b.add(new JLabel(SimpleGUIEditor.getDisplayName("max")));
        b.add(makeSpinner((Text)XPathAPI.selectSingleNode(element, "max/text()")));
        b.add(Box.createHorizontalGlue());
        return b;
    }

    private JSpinner makeSpinner(final Text text){
        final JSpinner spin = new JSpinner(new SpinnerNumberModel(Double.parseDouble(text.getNodeValue()),
                                                                  0, 10, .1));
        spin.addChangeListener(new ChangeListener(){
                    public void stateChanged(ChangeEvent e) {
                        text.setNodeValue(spin.getValue().toString());
                    }
                });
        return spin;
    }

}

