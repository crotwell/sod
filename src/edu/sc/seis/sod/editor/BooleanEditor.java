/**
 * OREditor.java
 *
 * @author Charles Groves
 */

package edu.sc.seis.sod.editor;
import java.util.HashMap;
import java.util.Map;
import javax.swing.Box;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import org.apache.xpath.XPathAPI;
import org.w3c.dom.Element;



public class BooleanEditor implements EditorPlugin{
    public BooleanEditor(SodGUIEditor editor){
        this.editor = editor;
    }

    public JComponent getGUI(Element element) throws Exception {
        String name = element.getTagName();
        String type = getType(name);
        if(type.equals(NOT) || type.equals(AND_WRAPPER) || type.equals(OR_WRAPPER)){
            Box b = Box.createHorizontalBox();
            //b.add(new JLabel(NOT + " "));
            Element child = (Element)XPathAPI.selectSingleNode(element, "*");
            b.add(editor.getCompForElement(child));
            if (type.equals(NOT)) {
                b.setBorder(new TitledBorder("Not"));
            } else if (type.equals(AND_WRAPPER)) {
                b.setBorder(new TitledBorder("All must match"));
            } else if (type.equals(AND_WRAPPER)) {
                b.setBorder(new TitledBorder("At least one must match"));
            }
            return b;
        }else{
            Box b = Box.createVerticalBox();
            JComponent[] comps = editor.getCompsForNodeList(element.getChildNodes());
            for (int i = 0; i < comps.length - 1; i++) {
                b.add(comps[i]);
                Box typeBox = Box.createHorizontalBox();
                typeBox.add(new JLabel(type));
                typeBox.add(Box.createHorizontalGlue());
                b.add(typeBox);
            }
            b.add(comps[comps.length - 1]);
            String ssType = name.substring(0, name.length() - type.length());
            Inserter ins = (Inserter)inserters.get(ssType);
            if (ins == null) {
                ins =new Inserter(ssType, editor);
                inserters.put(ssType, ins);
            }
            b.add(ins.getGUI(element));
            if(type == AND){ b.setBorder(AND_BORDER);}
            else{ b.setBorder(OR_BORDER); }
            return b;
        }
    }

    String getType(String name) {
        String type = "UNKNOWN";
        if(name.endsWith(OR)){
            type = OR;
        }else if(name.endsWith(AND)){
            type = AND;
        }else if(name.endsWith(NOT)){
            type = NOT;
        }else if(name.endsWith(XOR)){
            type = XOR;
        }else if(name.startsWith(AND)){
            type = AND_WRAPPER;
        }else if(name.startsWith(OR)){
            type = OR_WRAPPER;
        }
        return type;
    }

    private static Border AND_BORDER = new TitledBorder("All of these");

    private static Border OR_BORDER = new TitledBorder("Any of these");

    private Map inserters = new HashMap();
    private static final String AND = "AND", OR = "OR", NOT ="NOT", XOR = "XOR", AND_WRAPPER = "AND_WRAPPER", OR_WRAPPER= "OR_WRAPPER";
    private SodGUIEditor editor;
}

