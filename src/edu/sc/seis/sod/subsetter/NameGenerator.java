package edu.sc.seis.sod.subsetter;

import org.w3c.dom.*;
import edu.iris.Fissures.IfEvent.*;
import edu.iris.Fissures.model.MicroSecondDate;
import edu.sc.seis.fissuresUtil.display.ParseRegions;
import edu.sc.seis.sod.*;
import org.apache.log4j.*;
import java.text.SimpleDateFormat;
import java.util.TimeZone;

/**
 * NameGenerator.java
 *
 *
 * Created: Fri Mar 28 16:04:01 2003
 *
 * @author <a href="mailto:crotwell@owl.seis.sc.edu">Philip Crotwell</a>
 * @version 1.0
 */
public class NameGenerator {
    public NameGenerator(Element config) {
        this.config = config;
        regions = new ParseRegions();
    } // NameGenerator constructor

    public String getName(EventAccessOperations event) {
        NodeList children = config.getChildNodes();
        Node node;
        String name = "";
        String separator = "";
        String separatorValue = "_";
        for(int counter = 0; counter < children.getLength(); counter++ ) {
            node = children.item(counter);
            if(node instanceof Element ) {
                if(((Element)node).getTagName().equals("separator")) {
                    separatorValue = SodUtil.getText((Element)node);
                    if (separatorValue == null) {
                        separatorValue = "";
                    } // end of if (sep == null)
                }
            }
        }

        for(int counter = 0; counter < children.getLength(); counter++ ) {
            node = children.item(counter);
            if (node instanceof Text) {
                String text = ((Text)node).getData();
                text = text.trim();
                StringBuffer sb = new StringBuffer(text.length());
                char[] chars = text.toCharArray();
                for (int i=0; i<chars.length; i++) {
                    if (Character.isWhitespace(chars[i])) {
                        // skip
                    } else {
                        sb.append(chars[i]);
                    } // end of else
                } // end of for (int i=0; i<chars.length; i++)
                text = sb.toString();
                text = text.replace(' ','_');
                //      logger.debug("Breq text node:"+text);
                name += text;
            } else if(node instanceof Element ) {
                name += separator;
                separator = separatorValue;
                if(((Element)node).getTagName().equals("feRegionName")) {
                    String regionName =
                        regions.getRegionName(event.get_attributes().region);
                    regionName = regionName.replace(' ', '_');
                    regionName = regionName.replace(',', '_');
                    name += regionName;
                } else if(((Element)node).getTagName().equals("feRegionNumber")) {
                    int regionNum =
                        event.get_attributes().region.number;
                    name += regionNum;
                } else if(((Element)node).getTagName().equals("depth")) {
                    try {
                        name +=
                            event.get_preferred_origin().my_location.depth.value;
                    } catch (NoPreferredOrigin e) {
                    } // end of try-catch
                } else if(((Element)node).getTagName().equals("latitude")) {
                    try {
                        name +=
                            event.get_preferred_origin().my_location.latitude;
                    } catch (NoPreferredOrigin e) {
                    } // end of try-catch
                } else if(((Element)node).getTagName().equals("longitude")) {
                    try {
                        name +=
                            event.get_preferred_origin().my_location.longitude;
                    } catch (NoPreferredOrigin e) {
                    } // end of try-catch
                } else if(((Element)node).getTagName().equals("magnitude")) {
                    try {
                        Magnitude[] mags =
                            event.get_preferred_origin().magnitudes;
                        if (mags.length > 0) {
                            name +=
                                mags[0].value;
                        } // end of if (mags.length > 0)
                    } catch (NoPreferredOrigin e) {
                    } // end of try-catch
                } else if(((Element)node).getTagName().equals("originTime")) {
                    try {
                        String formatStr = SodUtil.getText((Element)node);
                        //logger.debug("Breqfast label originTime:"+formatStr);
                        if (formatStr.length() == 0) {
                            formatStr = "yyyyMMdd'T'HHmmss.SSS";
                        } // end of if (formatStr.length == 0)
            
                        SimpleDateFormat labelFormat = new SimpleDateFormat(formatStr);
                        labelFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
                        MicroSecondDate msd =
                            new MicroSecondDate(event.get_preferred_origin().origin_time);
                        name +=
                            labelFormat.format(msd);
                    } catch (NoPreferredOrigin e) {
                    } // end of try-catch
                } else if(((Element)node).getTagName().equals("separator")) {
                    // ignore as this was processed previously
                } else {
                    logger.warn("label tag "+((Element)node).getTagName()+" is not understood.");
                } // end of else
                
            }
        }
        name = name.replace(' ', '_');
        name = name.replace(',', '_');
        logger.debug("Breqfast label: "+name);
        return name;
    }
    
    public static String filize(String fileName){
        fileName = fileName.replace(' ', '_');
        fileName = fileName.replace(',', '_');
        fileName = fileName.replace('/', '_');
        fileName = fileName.replace(':', '_');
        return fileName;
    }

    Element config;

    ParseRegions regions;

    static Category logger =
        Category.getInstance(NameGenerator.class.getName());
    

} // NameGenerator
