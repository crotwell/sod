/**
 * SacFileEditor.java
 *
 * @author Created by Omnicore CodeGuide
 */

package edu.sc.seis.sod.editor;
import java.awt.BorderLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import org.w3c.dom.Element;



public class SacFileEditor implements EditorPlugin {

    public SacFileEditor(SodGUIEditor editor) {
        this.editor = editor;
    }

    public JComponent getGUI(Element element) throws Exception {
        JComponent comp = editor.getDefaultCompForElement(element);
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.add(comp, BorderLayout.CENTER);
        JButton gee = new JButton("GEE");
        panel.add(gee, BorderLayout.SOUTH);
        return panel;
    }

    SodGUIEditor editor;
}

