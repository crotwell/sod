/**
 * GeographicRegionEditor.java
 *
 * @author Charles Groves
 */

package edu.sc.seis.sod.editor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import org.apache.xpath.XPathAPI;
import org.w3c.dom.Element;
import org.w3c.dom.Text;
import edu.sc.seis.fissuresUtil.display.GeographicRegion;
import edu.sc.seis.fissuresUtil.display.ParseRegions;
import edu.sc.seis.fissuresUtil.display.SeismicRegion;



public class GeographicRegionEditor implements EditorPlugin{

    public JComponent getGUI(Element element) throws Exception {
        Box b = Box.createHorizontalBox();
        final ParseRegions pr = ParseRegions.getInstance();
        SeismicRegion[] allRegions = pr.getAlphabetizedSeismicRegions();
        final Text t = (Text)XPathAPI.selectSingleNode(element, "text()");
        String curValue = t.getData();
        int curIntValue = Integer.parseInt(curValue);
        final JButton activator = new JButton(pr.getGeographicRegion(curIntValue).getName());
        final JPopupMenu p = new JPopupMenu();
        for (int i = 0; i < allRegions.length; i++) {
            JMenu seisRegionMenu = new JMenu(allRegions[i].getName());
            GeographicRegion[] geoRegions = allRegions[i].getChildren();
            for (int j = 0; j < geoRegions.length; j++) {
                final JMenuItem regionItem = new JMenuItem(geoRegions[j].getName());
                seisRegionMenu.add(regionItem);
                regionItem.addActionListener(new ActionListener(){
                            public void actionPerformed(ActionEvent e) {
                                String region = regionItem.getText();
                                int val = pr.getRegionValue(region);
                                t.setData("" + val);
                                activator.setText(region);
                            }
                        });
            }
            p.add(seisRegionMenu);
        }
        activator.addActionListener(new ActionListener(){
                    public void actionPerformed(ActionEvent e) {
                        p.show((JButton)e.getSource(), 0, 0);
                    }
                });
        b.add(EditorUtil.getLabel(element.getTagName()));
        b.add(activator);
        b.add(Box.createHorizontalGlue());
        return b;
    }


}

