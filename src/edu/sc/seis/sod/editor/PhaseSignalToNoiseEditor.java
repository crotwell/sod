/*
 * Created on Jul 29, 2004
 */
package edu.sc.seis.sod.editor;

import java.awt.BorderLayout;
import javax.swing.Box;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;
import javax.xml.transform.TransformerException;
import org.apache.xpath.XPathAPI;
import org.w3c.dom.Element;

/**
 * @author oliverpa
 */
public class PhaseSignalToNoiseEditor implements EditorPlugin {

    public JComponent getGUI(Element element) throws TransformerException {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.setBorder(new TitledBorder(SodGUIEditor.getDisplayName(element.getTagName())));
        Box b = Box.createHorizontalBox();
        b.add(Box.createHorizontalGlue());
        b.add(EditorUtil.getLabeledTextField((Element)(XPathAPI.selectSingleNode(element, "modelName"))));
        b.add(Box.createHorizontalStrut(10));
        b.add(EditorUtil.getLabeledTextField((Element)(XPathAPI.selectSingleNode(element, "phaseName"))));
        b.add(Box.createHorizontalGlue());
        panel.add(b, BorderLayout.NORTH);

        b = Box.createHorizontalBox();
        b.setBorder(new TitledBorder("Short Offset"));
        b.add(Box.createHorizontalGlue());
        b.add(new JLabel("Begin:"));
        b.add(EditorUtil.makeTimeIntervalTwiddler((Element)XPathAPI.selectSingleNode(element, "shortOffsetBegin"), null, null));
        b.add(Box.createHorizontalStrut(10));
        b.add(new JLabel("End:"));
        b.add(EditorUtil.makeTimeIntervalTwiddler((Element)XPathAPI.selectSingleNode(element, "shortOffsetEnd"), null, null));
        b.add(Box.createHorizontalGlue());
        panel.add(b, BorderLayout.CENTER);

        b = Box.createHorizontalBox();
        b.setBorder(new TitledBorder("Long Offset"));
        b.add(Box.createHorizontalGlue());
        b.add(new JLabel("Begin:"));
        b.add(EditorUtil.makeTimeIntervalTwiddler((Element)XPathAPI.selectSingleNode(element, "longOffsetBegin"), null, null));
        b.add(Box.createHorizontalStrut(10));
        b.add(new JLabel("End:"));
        b.add(EditorUtil.makeTimeIntervalTwiddler((Element)XPathAPI.selectSingleNode(element, "longOffsetEnd"), null, null));
        b.add(Box.createHorizontalGlue());
        panel.add(b, BorderLayout.SOUTH);

        return panel;
    }

}
