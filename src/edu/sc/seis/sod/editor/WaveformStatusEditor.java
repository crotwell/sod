/**
 * WaveformStationStatusEditor.java
 *
 * @author Created by Omnicore CodeGuide
 */

package edu.sc.seis.sod.editor;
import org.w3c.dom.Element;
import javax.swing.JComponent;



public class WaveformStatusEditor implements EditorPlugin {

    public JComponent getGUI(Element element) throws Exception {
        return EditorUtil.getBoxWithLabel(element);
    }

}

