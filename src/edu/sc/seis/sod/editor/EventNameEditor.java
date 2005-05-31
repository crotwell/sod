package edu.sc.seis.sod.editor;

import javax.swing.JComponent;
import org.w3c.dom.Element;

/**
 * @author oliverpa Created on May 23, 2005
 */
public class EventNameEditor implements EditorPlugin {

    public EventNameEditor(SodGUIEditor sodEditor) {
        this.sodEditor = sodEditor;
    }

    public JComponent getGUI(Element element) throws Exception {
        return EditorUtil.getLabeledTextField(element,
                                              sodEditor.getDisplayName(element.getTagName()));
    }

    private SodGUIEditor sodEditor;
}