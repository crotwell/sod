/**
 * ResponseWriterEditor.java
 *
 * @author Philip Crotwell
 */

package edu.sc.seis.sod.editor;
import edu.sc.seis.sod.SodUtil;
import javax.swing.Box;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.border.TitledBorder;
import org.w3c.dom.Element;



public class ResponseWriterEditor  implements EditorPlugin {

    public JComponent getGUI(Element element) throws Exception {
        Box b = Box.createVerticalBox();
        Element respType = SodUtil.getElement(element, "type");
        if (respType != null) {
            b.add(EditorUtil.getLabeledComboBox(respType, new String[] {"polezero", "resp"}));
        }
        b.setBorder(new TitledBorder(SimpleGUIEditor.getDisplayName(element.getTagName())));
        Element dirElement = SodUtil.getElement(element, "directory");
        b.add(EditorUtil.getLabeledTextField(dirElement));
        Box hBox = Box.createHorizontalBox();
        hBox.add(EditorUtil.getLabel("filePattern"));
        hBox.add(new JLabel("<networkCode/>.<stationCode/>.<siteCode/>.<channelCode/>.resp"));
        b.add(hBox);
        return b;
    }

}

