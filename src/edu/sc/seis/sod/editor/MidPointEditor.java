/**
 * MidPointEditor.java
 *
 * @author Created by Omnicore CodeGuide
 */

package edu.sc.seis.sod.editor;

import java.awt.BorderLayout;
import java.util.LinkedList;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class MidPointEditor extends Switcher implements EditorPlugin {

    public MidPointEditor(SodGUIEditor editor){
        super( allAreas, editor);
        this.editor = editor;
    }

    public JComponent getGUI(Element element) throws Exception {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.setBorder(new TitledBorder(SimpleGUIEditor.getDisplayName(element.getTagName())));
        NodeList nl = element.getChildNodes();
        int i=0;
        while( ! (nl.item(i) instanceof Element)) { i++; }
        panel.add(super.getGUI((Element)nl.item(i)), BorderLayout.CENTER);
        return panel;
    }

    private static LinkedList allAreas = new LinkedList();

    static {
        allAreas.add("boxArea");
        allAreas.add("pointDistanceArea");
        allAreas.add("globalArea");
    }
    private SodGUIEditor editor;
}

