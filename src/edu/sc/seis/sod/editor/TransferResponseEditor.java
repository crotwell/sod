package edu.sc.seis.sod.editor;

import javax.swing.Box;
import javax.swing.JComponent;
import javax.swing.border.TitledBorder;
import org.w3c.dom.Element;
import org.w3c.dom.Text;
import edu.sc.seis.fissuresUtil.display.configuration.DOMHelper;
import edu.sc.seis.sod.process.waveform.TransferResponse;

public class TransferResponseEditor implements EditorPlugin {

    public JComponent getGUI(Element element) throws Exception {
        Box hBox = Box.createHorizontalBox();
        hBox.setBorder(new TitledBorder("Transfer Response"));
        addField("lowCut",
                 TransferResponse.DEFAULT_LOW_CUT,
                 element,
                 hBox,
                 true);
        addField("lowPass",
                 TransferResponse.DEFAULT_LOW_PASS,
                 element,
                 hBox,
                 true);
        addField("highPass",
                 TransferResponse.DEFAULT_HIGH_PASS,
                 element,
                 hBox,
                 true);
        addField("highCut",
                 TransferResponse.DEFAULT_HIGH_CUT,
                 element,
                 hBox,
                 false);
        return hBox;
    }

    private static void addField(String name,
                                 float defaultValue,
                                 Element config,
                                 Box mainBox,
                                 boolean appendStrut) {
        Text text = DOMHelper.getTextChildFromPossiblyNonexistentElement(config,
                                                                         name,
                                                                         ""
                                                                                 + defaultValue);
        mainBox.add(EditorUtil.getLabeledTextField(text, name));
        if(appendStrut) {
            mainBox.add(Box.createHorizontalStrut(10));
        }
    }
}
