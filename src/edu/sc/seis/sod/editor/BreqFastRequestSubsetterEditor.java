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
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

/**
 * @author oliverpa
 */
public class BreqFastRequestSubsetterEditor implements EditorPlugin {

	public JComponent getGUI(Element element) throws TransformerException {
		Box box = Box.createVerticalBox();
		box.setBorder(new TitledBorder(SimpleGUIEditor.getDisplayName(element
				.getTagName())));
		box.add(EditorUtil.getLabeledTextField((Element) (XPathAPI
				.selectSingleNode(element, "dataDirectory"))));

		Box b = Box.createHorizontalBox();
		b.setBorder(new TitledBorder("Data Label"));
		b.add(Box.createHorizontalGlue());
		Box origBox = Box.createHorizontalBox();
		origBox.setBorder(new TitledBorder("Origin Format"));
		Text text2 = (Text) XPathAPI.selectSingleNode(element,
				"label/originTime/text()");
		origBox.add(EditorUtil.getTextField(text2));
		b.add(origBox);
		Box innerBox = Box.createHorizontalBox();
		innerBox.setBorder(new TitledBorder("Suffix"));
		//having trouble getting this text node...doing funky stuff to make it work
		NodeList nodes = XPathAPI.selectNodeList(element, "label/text()");
		text2 = (Text) nodes.item(nodes.getLength() - 1);
		innerBox.add(EditorUtil.getTextField(text2));
		b.add(innerBox);
		b.add(Box.createHorizontalGlue());
		box.add(b);

		box.add(EditorUtil.getLabeledTextField((Element) (XPathAPI
				.selectSingleNode(element, "name"))));
		box.add(EditorUtil.getLabeledTextField((Element) (XPathAPI
				.selectSingleNode(element, "inst"))));
		box.add(EditorUtil.getLabeledTextField((Element) (XPathAPI
				.selectSingleNode(element, "mail"))));
		box.add(EditorUtil.getLabeledTextField((Element) (XPathAPI
				.selectSingleNode(element, "email"))));
		box.add(EditorUtil.getLabeledTextField((Element) (XPathAPI
				.selectSingleNode(element, "phone"))));
		box.add(EditorUtil.getLabeledTextField((Element) (XPathAPI
				.selectSingleNode(element, "fax"))));
		box.add(EditorUtil.getLabeledTextField((Element) (XPathAPI
				.selectSingleNode(element, "media"))));
		box.add(EditorUtil.getLabeledTextField((Element) (XPathAPI
				.selectSingleNode(element, "altmedia1"))));
		box.add(EditorUtil.getLabeledTextField((Element) (XPathAPI
				.selectSingleNode(element, "altmedia2"))));
		box.add(EditorUtil.getLabeledTextField((Element) (XPathAPI
				.selectSingleNode(element, "quality"))));
		return box;
	}

}