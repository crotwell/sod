/**
 * SubelementEater.java
 *
 * @author Charles Groves
 */

package edu.sc.seis.sod.editor;
import javax.swing.Box;
import javax.swing.JComponent;
import javax.swing.JLabel;
import org.w3c.dom.Element;



public class SubelementEater implements EditorPlugin{
    public JComponent getGUI(Element element) throws Exception {
        Box b = Box.createHorizontalBox();
        b.add(Box.createHorizontalStrut(10));
        b.add(new JLabel(SimpleGUIEditor.getDisplayName(element.getTagName())));
        b.add(Box.createHorizontalGlue());
        return b;
    }
}

