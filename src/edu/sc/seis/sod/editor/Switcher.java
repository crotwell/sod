/**
 * Switcher.java
 *
 * @author Charles Groves
 */

package edu.sc.seis.sod.editor;

import javax.swing.*;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Iterator;
import org.w3c.dom.Element;

public class Switcher extends TagChooser{
    public Switcher(String ssType, SodGUIEditor editor){
        super(ssType, editor);
    }

    public JComponent getGUI(Element element) throws Exception {
        // this is a hack to avoid same-named tags in the eventFinder element
        // need a real fix, but this is ok for short term
        if (element.getParentNode().getNodeName().equals("eventFinder")) {
            if (editor.getCustomEditor(element.getTagName()+PLUGIN_SUFFIX) != null) {
                return editor.getCustomEditor(element.getTagName()+PLUGIN_SUFFIX).getGUI(element);
            }
            return editor.getDefaultCompForElement(element);
        }

        ImageIcon changeIcon = new ImageIcon(TagChooser.class.getClassLoader().getResource("edu/sc/seis/sod/editor/change.gif"));
        JLabel change=  new JLabel(changeIcon);
        addPopup(change, element);
        Box changeHolder = Box.createVerticalBox();
        changeHolder.add(change);
        changeHolder.add(Box.createVerticalGlue());
        Box b = Box.createHorizontalBox();
        b.add(changeHolder);
        EditorPlugin plugin = editor.getCustomEditor(element.getTagName()+PLUGIN_SUFFIX);
        JComponent comp;
        if (plugin != null) {
            comp = plugin.getGUI(element);
        } else {
            comp = editor.getDefaultCompForElement(element);
        }
        b.add(comp);
        return b;
    }
}

