/**
 * CatalogEditor.java
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
import org.apache.xpath.XPathAPI;
import org.w3c.dom.Element;
import org.w3c.dom.Text;



public class CatalogEditor implements EditorPlugin{
    public JComponent getGUI(Element element) throws Exception {
        Box b = Box.createHorizontalBox();
        b.add(Box.createHorizontalGlue());
        b.add(new JLabel(SimpleGUIEditor.getDisplayName(element.getTagName())));
        b.add(EditorUtil.getComboBox(element, presetCatalogs));
        b.add(Box.createHorizontalGlue());
        return b;
    }

    private static final String[] presetCatalogs = new String[]{"WHDF", "PREF", "FINGER"};
}

