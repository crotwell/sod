/**
 * TextItemListener.java
 *
 * @author Created by Omnicore CodeGuide
 */

package edu.sc.seis.sod.editor;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import javax.swing.JComboBox;
import org.w3c.dom.Text;



public class TextItemListener implements ItemListener {
    public TextItemListener(Text text) {
        this.text = text;
    }

    protected Text text;

    public void itemStateChanged(ItemEvent e) {
        Object item = ((JComboBox)e.getSource()).getSelectedItem();
        text.setNodeValue((String)item);
    }
}
