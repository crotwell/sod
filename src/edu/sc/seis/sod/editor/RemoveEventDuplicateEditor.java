package edu.sc.seis.sod.editor;

import java.awt.BorderLayout;
import javax.swing.Box;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import edu.sc.seis.sod.SodUtil;

/**
 * @author oliverpa Created on Sep 21, 2004
 */
public class RemoveEventDuplicateEditor implements EditorPlugin {

    public JComponent getGUI(Element element) throws Exception {
        JPanel panel = new JPanel();
        panel.setBorder(new TitledBorder(SodGUIEditor.getDisplayName(element.getTagName())));
        Box b;
        panel.setLayout(new BorderLayout());
        NodeList kids = element.getChildNodes();
        for(int i = 0; i < kids.getLength(); i++) {
            if(kids.item(i) instanceof Element) {
                Element el = (Element)kids.item(i);
                if(el.getTagName().equals("timeVariance")) {
                    b = Box.createHorizontalBox();
                    b.add(new JLabel(SodGUIEditor.getDisplayName(el.getTagName())));
                    b.add(Box.createHorizontalStrut(10));
                    b.add(EditorUtil.makeQuantityTwiddler(el,
                                                          SodUtil.TIME_UNITS));
                    panel.add(b, BorderLayout.NORTH);
                } else if(el.getTagName().equals("depthVariance")
                        || el.getTagName().equals("distanceVariance")) {
                    b = Box.createHorizontalBox();
                    b.add(new JLabel(SodGUIEditor.getDisplayName(el.getTagName())));
                    b.add(Box.createHorizontalStrut(10));
                    b.add(EditorUtil.makeQuantityTwiddler(el,
                                                          SodUtil.LENGTH_UNITS));
                    if(el.getTagName().equals("depthVariance")) {
                        panel.add(b, BorderLayout.CENTER);
                    } else {
                        panel.add(b, BorderLayout.SOUTH);
                    }
                }
            }
        }
        return panel;
    }
}