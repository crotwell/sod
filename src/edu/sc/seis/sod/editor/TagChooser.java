/**
 * TagChooser.java
 *
 * @author Created by Omnicore CodeGuide
 */

package edu.sc.seis.sod.editor;

import java.util.Iterator;
import java.util.Vector;
import javax.swing.Box;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.event.ListSelectionListener;
import org.w3c.dom.Element;
import javax.swing.event.ListSelectionEvent;

public class TagChooser implements EditorPlugin {

    public TagChooser(String ssType, SodGUIEditor editor) {
        this.editor = editor;
        this.subsetterType = ssType+"NOT";
        ElementNode node = editor.getGrammar().getNode(subsetterType);
        if (node == null) {
            throw new NullPointerException("Couldn't get ElementNode for "+subsetterType);
        }
        Iterator it = node.childIterator();
        while (it.hasNext()) {
            subTypes.add(((ElementNode)it.next()).getName());
        }
    }

    public JComponent getGUI(Element element) throws Exception {
        // this is a hack to avoid same-named tags in the eventFinder element
        // need a real fix, but this is ok for short term
        if (element.getParentNode().getNodeName().equals("eventFinder")) {
            return editor.getDefaultCompForElement(element);
        }

        Box b = Box.createVerticalBox();
        JComboBox combo = new JComboBox(subTypes);
        combo.setSelectedItem(element.getTagName());
        b.add(combo);

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

    public Vector getSubTypes() {
        return subTypes;
    }

    public static final String PLUGIN_SUFFIX = "_tagChooser";

    SodGUIEditor editor;

    String subsetterType;

    Vector subTypes = new Vector();

    class ComboElementReset implements ListSelectionListener {

        ComboElementReset(Element e) {
            current = e;
        }

        public void valueChanged(ListSelectionEvent e) {
            String tagName = (String)((JComboBox)e.getSource()).getSelectedItem();
        }

        Element current;
    }
}

