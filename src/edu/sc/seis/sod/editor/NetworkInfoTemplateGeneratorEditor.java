/**
 * NetworkInfoTemplateGenerator.java
 *
 * @author Created by Omnicore CodeGuide
 */

package edu.sc.seis.sod.editor;
import java.awt.BorderLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;
import org.w3c.dom.Element;



public class NetworkInfoTemplateGeneratorEditor implements EditorPlugin {

    public JComponent getGUI(Element element) throws Exception {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.setBorder(new TitledBorder(SimpleGUIEditor.getDisplayName(element.getTagName())));
        panel.add(new JLabel());
        return panel;
    }

}

