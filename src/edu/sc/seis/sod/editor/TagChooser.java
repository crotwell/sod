/**
 * TagChooser.java
 *
 * @author Created by Omnicore CodeGuide
 */

package edu.sc.seis.sod.editor;

import javax.swing.*;

import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.w3c.dom.Element;

public abstract class TagChooser implements EditorPlugin {

    public TagChooser(String ssType, SodGUIEditor editor) {
        this.editor = editor;
        String subsetterType = ssType+"NOT";
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

    public TagChooser(List subTypes, SodGUIEditor editor) {
        this.editor = editor;
        this.subTypes = subTypes;
        Collections.sort(subTypes);
    }

    protected void addPopup(JComponent popper, Element element){
        final JPopupMenu popup = new JPopupMenu();
        ButtonGroup popupGroup = new ButtonGroup();
        Iterator it = getSubTypes().iterator();
        while(it.hasNext()) {
            String ssType = (String)it.next();
            JRadioButtonMenuItem menuItem = new JRadioButtonMenuItem(editor.getDisplayName(ssType));
            popupGroup.add(menuItem);
            if (ssType.equals(element.getTagName())) {
                menuItem.setSelected(true);
            }
            popup.add(menuItem);
        }
        popper.addMouseListener(new MouseAdapter() {
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
    }

    public List getSubTypes() { return subTypes; }

    public static final String PLUGIN_SUFFIX = "_tagChooser";

    protected SodGUIEditor editor;

    private List subTypes = new LinkedList();

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



