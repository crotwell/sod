package edu.sc.seis.sod.subsetter;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import edu.sc.seis.seisFile.fdsnws.stationxml.Equipment;
import edu.sc.seis.sod.SodUtil;

public class AbstractEquipmentSubsetter {

    public AbstractEquipmentSubsetter() {
        // TODO Auto-generated constructor stub
    }
    
    public AbstractEquipmentSubsetter(Element config) {
        parseConfig(config);
    }
    

    protected void parseConfig(Element config) {
        NodeList children = config.getChildNodes();
        for(int i = 0; i < children.getLength(); i++) {
            if(children.item(i) instanceof Element) {
                Element el = (Element)children.item(i);
                String tagName = el.getTagName();
                if(tagName.equals("type")) {
                    type = Pattern.compile(SodUtil.getNestedText(el));
                } else if(tagName.equals("manufacturer")) {
                    manufacturer = Pattern.compile(SodUtil.getNestedText(el));
                } else if(tagName.equals("vendor")) {
                    vendor = Pattern.compile(SodUtil.getNestedText(el));
                } else if(tagName.equals("model")) {
                    model = Pattern.compile(SodUtil.getNestedText(el));
                } else if(tagName.equals("serialNumber")) {
                    serialNumber = Pattern.compile(SodUtil.getNestedText(el));
                }
            }
        }
    }
    
    protected boolean doesMatch(Equipment eq) {
        return doesMatch(type, eq.getType())
                && doesMatch(manufacturer, eq.getManufacturer())
                && doesMatch(vendor, eq.getVendor())
                && doesMatch(model, eq.getModel())
                && doesMatch(serialNumber, eq.getSerialNumber());
    }
        
    protected boolean doesMatch(Pattern p, String val) {
        if (p == null) {
            return true; // null pattern matches all
        }
        Matcher m = p.matcher(val);
        return m.matches();
    }

    Pattern type;
    Pattern manufacturer;
    Pattern vendor;
    Pattern model;
    Pattern serialNumber;
}
