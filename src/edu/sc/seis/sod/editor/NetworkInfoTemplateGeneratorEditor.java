/**
 * NetworkInfoTemplateGenerator.java
 *
 * @author Created by Omnicore CodeGuide
 */

package edu.sc.seis.sod.editor;
import javax.swing.JComponent;
import org.w3c.dom.Element;



public class NetworkInfoTemplateGeneratorEditor implements EditorPlugin {
    public JComponent getGUI(Element element) throws Exception {
        return EditorUtil.getBoxWithLabel(element);
    }
}

