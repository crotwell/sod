/**
 * TagChooser.java
 *
 * @author Created by Omnicore CodeGuide
 */

package edu.sc.seis.sod.editor;

import edu.sc.seis.fissuresUtil.exceptionHandler.GlobalExceptionHandler;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.swing.ButtonGroup;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.w3c.dom.Element;

public abstract class TagChooser implements EditorPlugin {
    public TagChooser(String ssType, SodGUIEditor editor) {
        this.editor = editor;
        String subsetterType = ssType+"NOT";
        ElementNode node = (ElementNode)visitedMap.get(subsetterType);
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
                            popup.show(e.getComponent(),
                                       e.getX(),
                                       e.getY());
                        }
                    }
                });
    }

    public List getSubTypes() { return subTypes; }

    public static final String PLUGIN_SUFFIX = "_tagChooser";

    protected SodGUIEditor editor;

    private List subTypes = new LinkedList();

    private static Map visitedMap;
    static{
        try {
            ClassLoader cl = TagChooser.class.getClassLoader();
            InputStream is = cl.getResourceAsStream(SchemaGrammar.NODE_JAR_LOC);
            ObjectInputStream ois = new ObjectInputStream(is);
            visitedMap = (Map)ois.readObject();
        } catch (Exception e) {
            GlobalExceptionHandler.handle(e);
        }
    }

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



