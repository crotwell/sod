/**
 * SeismicRegionEditor.java
 *
 * @author Charles Groves
 */

package edu.sc.seis.sod.editor;
import edu.sc.seis.fissuresUtil.display.ParseRegions;
import edu.sc.seis.fissuresUtil.display.SeismicRegion;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.Collator;
import java.util.Arrays;
import java.util.Locale;
import javax.swing.Box;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import org.apache.xpath.XPathAPI;
import org.w3c.dom.Element;
import org.w3c.dom.Text;



public class SeismicRegionEditor implements EditorPlugin{
    public JComponent getGUI(Element element) throws Exception {
        final ParseRegions pr = ParseRegions.getInstance();
        SeismicRegion[] regions = pr.getAlphabetizedSeismicRegions();
        final JComboBox selections = new JComboBox(regions);
        final Text value = (Text)XPathAPI.selectSingleNode(element, "text()");
        int region = Integer.parseInt(value.getData());
        selections.setSelectedItem(pr.getSeismicRegionName(region));
        selections.addActionListener(new ActionListener(){
                    public void actionPerformed(ActionEvent e) {
                        String newRegion = (String)selections.getSelectedItem();
                        int regionValue = pr.getSeismicRegionValue(newRegion);
                        value.setData("" + regionValue);
                    }
                });
        Box b = Box.createHorizontalBox();
        b.add(new JLabel(SimpleGUIEditor.getDisplayName(element.getTagName())));
        b.add(selections);
        b.add(Box.createHorizontalGlue());
        return b;
    }


}

