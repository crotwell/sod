/**
 * BoxAreaEditor.java
 *
 * @author Created by Omnicore CodeGuide
 */

package edu.sc.seis.sod.editor;

import java.awt.BorderLayout;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;
import org.apache.xpath.XPathAPI;
import org.w3c.dom.Element;

public class BoxAreaEditor  implements EditorPlugin {

    public BoxAreaEditor(SodGUIEditor editor){
        this.editor = editor;
    }

    public JComponent getGUI(Element element) throws Exception {

        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.setBorder(new TitledBorder(SimpleGUIEditor.getDisplayName(element.getTagName())));

        Element el = (Element)XPathAPI.selectSingleNode(element, "latitudeRange");
        panel.add(editor.getCompForElement(el), BorderLayout.NORTH);

        el = (Element)XPathAPI.selectSingleNode(element, "longitudeRange");
        panel.add(editor.getCompForElement(el), BorderLayout.SOUTH);

        return panel;
    }

    private SodGUIEditor editor;
}

