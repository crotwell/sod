/*
 * Created on Jul 29, 2004
 */
package edu.sc.seis.sod.editor;

import javax.swing.Box;
import javax.swing.JComponent;
import javax.swing.border.TitledBorder;
import javax.xml.transform.TransformerException;

import org.apache.xpath.XPathAPI;
import org.w3c.dom.Element;

/**
 * @author oliverpa
 */
public class LegacyExecuteEditor implements EditorPlugin{

	public JComponent getGUI(Element el) throws TransformerException {
		Box box = Box.createHorizontalBox();
		box.setBorder(new TitledBorder(SimpleGUIEditor.getDisplayName(el.getTagName())));
		box.add(EditorUtil.getLabeledTextField((Element)(XPathAPI.selectSingleNode(el, "command"))));
		return box;
	}
	
}
