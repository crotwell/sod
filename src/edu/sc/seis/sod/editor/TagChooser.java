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
import javax.xml.transform.TransformerException;
import org.w3c.dom.Element;

public class TagChooser implements EditorPlugin {

    public TagChooser(String ssType, SodGUIEditor editor) {
        this.editor = editor;
        this.subsetterType = ssType+"NOT";
        ElementNode node = editor.getGrammer().getNode(subsetterType);
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

        b.add(combo);
        EditorPlugin plugin = editor.getCustomEditor(element.getTagName()+"_tagChooser");
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

    SodGUIEditor editor;

    String subsetterType;

    Vector subTypes = new Vector();
}

