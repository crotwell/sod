/**
 * MagnitudeEditor.java
 *
 * @author Charles Groves
 */

package edu.sc.seis.sod.editor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.Box;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.border.TitledBorder;
import org.apache.xpath.XPathAPI;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.Text;



public class MagnitudeEditor implements EditorPlugin{
    public JComponent getGUI(Element element) throws Exception {
        Box b = Box.createHorizontalBox();
        b.setBorder(new TitledBorder(SimpleGUIEditor.getDisplayName(element.getTagName())));
        b.add(Box.createHorizontalGlue());

        Node node = XPathAPI.selectSingleNode(element, "magType/text()");
        if (node != null){
            b.add(new JLabel(SimpleGUIEditor.getDisplayName("magType")));
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
        }

        node = XPathAPI.selectSingleNode(element, "min/text()");
        if (node != null){
            b.add(new JLabel(SimpleGUIEditor.getDisplayName("min")));
            b.add(EditorUtil.createNumberSpinner((Text)node, 0, 10, .1));
            b.add(Box.createHorizontalGlue());
        }

        node = XPathAPI.selectSingleNode(element, "max/text()");
        if (node != null){
            b.add(new JLabel(SimpleGUIEditor.getDisplayName("max")));
            b.add(EditorUtil.createNumberSpinner((Text)XPathAPI.selectSingleNode(element, "max/text()"), 0, 10, .1));
        }

        b.add(Box.createHorizontalGlue());
        return b;
    }
}


