package edu.sc.seis.sod.editor;

import javax.swing.Box;
import javax.swing.JComponent;
import javax.swing.border.TitledBorder;
import org.w3c.dom.Element;
import edu.sc.seis.sod.SodUtil;


/**
 * @author oliverpa
 * Created on May 20, 2005
 */
public class WrapperEditor implements EditorPlugin {

    public WrapperEditor(SodGUIEditor sodEditor) {
        this.sodEditor = sodEditor;
    }
    
    public JComponent getGUI(Element element) throws Exception {
        Box b = Box.createVerticalBox();
        b.setBorder(new TitledBorder(sodEditor.getDisplayName(element.getTagName())));
        b.add(sodEditor.getCompForElement(SodUtil.getFirstEmbeddedElement(element)));
        return b;
    }
    
    private SodGUIEditor sodEditor;
}
