/**
 * RefreshIntervalEditor.java
 *
 * @author Created by Omnicore CodeGuide
 */

package edu.sc.seis.sod.editor;
import javax.swing.Box;
import javax.swing.JComponent;
import javax.swing.border.TitledBorder;
import org.w3c.dom.Element;

public class RefreshIntervalEditor implements EditorPlugin{

    public RefreshIntervalEditor(){
        System.out.println("RefreshIntervalEditor created");
    }

    public JComponent getGUI(Element element) throws Exception {
        System.out.println("RefreshIntervalEditor.getGUI() called");
        Box b = Box.createVerticalBox();
        b.add(EditorUtil.makeTimeIntervalTwiddler(element));
        b.setBorder(new TitledBorder(SimpleGUIEditor.getDisplayName(element.getTagName())));
        return b;
    }
}

