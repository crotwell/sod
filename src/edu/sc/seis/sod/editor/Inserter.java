/**
 * Inserter.java
 *
 * @author Charles Groves
 */

package edu.sc.seis.sod.editor;
import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import org.w3c.dom.Element;



public class Inserter extends TagChooser{
    public Inserter(String ssType, SodGUIEditor editor){
        super(ssType, editor);
    }

    public JComponent getGUI(Element element) throws Exception {
        ImageIcon plusIcon = new ImageIcon(TagChooser.class.getClassLoader().getResource("edu/sc/seis/sod/editor/plus.gif"));
        JLabel plus =  new JLabel(plusIcon);
        Box plusHolder = Box.createHorizontalBox();
        plusHolder.add(plus);
        plusHolder.add(Box.createHorizontalGlue());
        addPopup(plus, element);
        return plusHolder;
    }
}

