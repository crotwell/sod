/**
 * CatalogEditor.java
 *
 * @author Charles Groves
 */

package edu.sc.seis.sod.editor;
import javax.swing.Box;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import org.w3c.dom.Element;



public class CatalogEditor implements EditorPlugin{
    public JComponent getGUI(Element element) throws Exception {
        Box b = Box.createHorizontalBox();
        b.add(Box.createHorizontalGlue());
        b.add(new JLabel(SimpleGUIEditor.getDisplayName(element.getTagName())));
        JComboBox comBox = EditorUtil.getComboBox(element, presetCatalogs);
        comBox.setEditable(true);
        b.add(comBox);
        
        b.add(Box.createHorizontalGlue());
        return b;
    }

    private static final String[] presetCatalogs = new String[]{"WHDF", "PREF", "FINGER"};
}

