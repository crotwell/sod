package edu.sc.seis.sod.editor;

import javax.swing.Box;
import javax.swing.JComponent;
import javax.swing.border.TitledBorder;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;


/**
 * @author oliverpa
 * Created on May 20, 2005
 */
public class EmbeddedEditor implements EditorPlugin {
    
    public EmbeddedEditor(SodGUIEditor sodEditor) {
        this.sodEditor = sodEditor;
    }

    public JComponent getGUI(Element element) throws Exception {
        Box b = Box.createVerticalBox();
        b.setBorder(new TitledBorder(sodEditor.getDisplayName(element.getTagName())));
        NodeList nl = element.getChildNodes();
        JComponent[] comps = sodEditor.getCompsForNodeList(nl);
        for(int i = 0; i < comps.length; i++) {
            b.add(comps[i]);
        }
        return b;
    }
    
    private SodGUIEditor sodEditor;
}
