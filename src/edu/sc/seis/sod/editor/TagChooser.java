/**
 * TagChooser.java
 *
 * @author Created by Omnicore CodeGuide
 */

package edu.sc.seis.sod.editor;

import javax.swing.*;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.w3c.dom.Element;

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
        Collections.sort(subTypes);
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

        Box b = Box.createHorizontalBox();
        JLabel replace = new JLabel(new ImageIcon(this.getClass().getClassLoader().getResource("edu/sc/seis/sod/editor/recycle.png")));
        final JPopupMenu popup = new JPopupMenu();
        ButtonGroup popupGroup = new ButtonGroup();
        Iterator it = subTypes.iterator();
        while(it.hasNext()) {
            String ssType = (String)it.next();
            JRadioButtonMenuItem menuItem = new JRadioButtonMenuItem(editor.getDisplayName(ssType));
            popupGroup.add(menuItem);
            if (ssType.equals(element.getTagName())) {
                menuItem.setSelected(true);
            }
            popup.add(menuItem);
        }
        replace.addMouseListener(new MouseAdapter() {
                    public void mousePressed(MouseEvent e) {
                        maybeShowPopup(e);
                    }

                    public void mouseReleased(MouseEvent e) {
                        maybeShowPopup(e);
                    }

                    private void maybeShowPopup(MouseEvent e) {
                        if (e.isPopupTrigger()) {
                            System.out.println("Show popup");
                            popup.show(e.getComponent(),
                                       e.getX(),
                                       e.getY());
                        } else {
                            System.out.println("No show popup");
                        }
                    }


                });
        Box vBox = Box.createVerticalBox();
        vBox.add(replace);
        vBox.add(Box.createGlue());
        b.add(vBox);

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

    public List getSubTypes() {
        return subTypes;
    }

    public static final String PLUGIN_SUFFIX = "_tagChooser";

    SodGUIEditor editor;

    String subsetterType;

    LinkedList subTypes = new LinkedList();

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

