package edu.sc.seis.sod.subsetter.waveFormArm;

import edu.sc.seis.sod.*;
import edu.sc.seis.fissuresUtil.display.ParseRegions;
import java.util.*;
import org.w3c.dom.*;
import edu.iris.Fissures.IfNetwork.*;
import edu.iris.Fissures.network.*;
import edu.iris.Fissures.IfEvent.*;
import edu.iris.Fissures.network.*;
import edu.iris.Fissures.IfSeismogramDC.*;
import edu.iris.Fissures.model.*;
import edu.iris.Fissures.*;
import org.apache.log4j.*;
import java.io.*;
import java.text.*;

/**
 * Creates a breqfast email requset file based on the events and channels that
 * sod finds. This is done as a EventChannelSubsetter because the data may 
 * not be available via a DHI server.
 * 
 *
 *
 * Created: Fri Oct 11 15:40:03 2002
 *
 * @author <a href="mailto:">Philip Crotwell</a>
 * @version
 */

public class BreqFastAvailableData  implements AvailableDataSubsetter, SodElement {
    public BreqFastAvailableData(Element config) {
	this.config = config;
	regions = new ParseRegions();
	String datadirName = getConfig("dataDirectory");
	this.dataDirectory = new File(datadirName);
	format.setTimeZone(TimeZone.getTimeZone("GMT"));
    }

    public boolean accept(EventAccessOperations event, 
			  NetworkAccess network, 
			  Channel channel, 
			  RequestFilter[] request, 
			  RequestFilter[] available, 
			  CookieJar cookies) {
	try {
	    writeToBFEmail(event, network, channel, request, cookies);
	} catch (IOException e) {
	} catch (ConfigurationException e) {
	    logger.error("Problem writting to breqfast file",e);
	} // end of try-catch
	
	// don't care if yes or no
	return true;
    }

    protected String getConfig(String name) {
	return SodUtil.getText(SodUtil.getElement(config, name));
    }

    protected synchronized void writeToBFEmail(EventAccessOperations event, 
					       NetworkAccess networkAccess, 
					       Channel channel,
					       RequestFilter[] request, 
					       CookieJar cookies) 
	throws IOException, ConfigurationException {
	if ( ! dataDirectory.exists()) {
	    if ( ! dataDirectory.mkdirs()) {
		throw new ConfigurationException("Unable to create directory."+dataDirectory);
	    } // end of if (!)
		
	} // end of if (dataDirectory.exits())
	
	File bfFile = new File(dataDirectory, getFileName(event));
	if (bfFile.exists()) {
	    fileExists = true;
	} else {
	    fileExists = false;
	} // end of else
	
	Writer out = new BufferedWriter(new FileWriter(bfFile.getAbsolutePath(), true));
	
	if ( ! fileExists) {
	    // first tme to this event, insert headers
	    out.write(".NAME "+getConfig("name")+nl);
	    out.write(".INST "+getConfig("inst")+nl);
	    out.write(".MAIL "+getConfig("mail")+nl);
	    out.write(".EMAIL "+getConfig("email")+nl);
	    out.write(".PHONE "+getConfig("phone")+nl);
	    out.write(".FAX "+getConfig("fax")+nl);
	    out.write(".MEDIA "+getConfig("media")+nl);
	    out.write(".ALTERNATE MEDIA "+getConfig("altmedia1")+nl);
	    out.write(".ALTERNATE MEDIA "+getConfig("altmedia2")+nl);
	    try {
		Origin o = event.get_preferred_origin();		 
	    out.write(".SOURCE "
		      +"~"+o.catalog
		      +" "+o.contributor
		      +"~unknown~unknown~"+nl);
	    MicroSecondDate oTime = new MicroSecondDate(o.origin_time);
	    out.write(".HYPO "
		      +"~"+format.format(oTime)
		      +tenths.format(oTime)
		      +"~"
		      +o.my_location.latitude
		      +"~"
		      +o.my_location.longitude
		      +"~"
		      +((QuantityImpl)o.my_location.depth).convertTo(UnitImpl.KILOMETER).getValue()
		      +"~"
		      +"0"
		      +"~"
		      +event.get_attributes().region.number
		      +"~"
		      +regions.getRegionName(event.get_attributes().region)
		      +"~"
		      +nl);
	    for (int j=0; j<o.magnitudes.length; j++) {
		out.write(".MAGNITUDE ~"+o.magnitudes[j].type+"~"+o.magnitudes[j].value+"~"+nl);
	    } // end of for (int j=0; j<o.magnitude.length; j++)
	    
	    } catch (NoPreferredOrigin e) {
		
	    } // end of try-catch
	    
	    out.write(".QUALITY "+getConfig("quality")+nl);
	    out.write(".LABEL "+getLabel(event)+nl);
	    out.write(".END"+nl);
	    out.write(nl);
	} // end of if ( ! fileExists)

	MicroSecondDate start, end;
	for (int i=0; i<request.length; i++) {
	    start = new MicroSecondDate(request[i].start_time);
	    end = new MicroSecondDate(request[i].end_time);
	
	    out.write(channel.my_site.my_station.get_code()
		      +" "+
		      channel.my_site.my_station.my_network.get_code()
		      +" "+
		      format.format(start)+tenths.format(start).substring(0,1)
		      +" "+
		      format.format(end)+tenths.format(end).substring(0,1)
		      +" 1 "+
		      channel.get_code()
		      +" "+
		      channel.my_site.get_code()
		      +nl);
	    
	} // end of for (int i=0; i<request.length; i++)
	out.close();
    }

    protected String getFileName(EventAccessOperations event) {
	return getLabel(event)+".breqfast";
    }

    protected String getLabel(EventAccessOperations event) {
	Element labelConfig = SodUtil.getElement(config, "label");
	if (labelConfig == null) {
	    String eventFileName = 
		regions.getRegionName(event.get_attributes().region);
	    try {
		eventFileName+=
		    " "+event.get_preferred_origin().origin_time.date_time;
	    } catch (NoPreferredOrigin e) {
		
	    } // end of try-catch
	    
	    eventFileName = eventFileName.replace(' ', '_');
	    eventFileName = eventFileName.replace(',', '_');
	    return eventFileName;
	} // end of if (labelConfig == null)
	
	// not null so use config
	NodeList children = labelConfig.getChildNodes();
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
		//		logger.debug("Breq text node:"+text);
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
		    logger.warn("BreqFast label, tag "+((Element)node).getTagName()+" is not understood.");
		} // end of else
		
	    }
	}
	name = name.replace(' ', '_');
	name = name.replace(',', '_');
	logger.debug("Breqfast label: "+name);
	return name;
    }

    static final String nl = "\n";

    SimpleDateFormat format = new SimpleDateFormat("yyyy MM dd HH mm ss.");
    SimpleDateFormat tenths = new SimpleDateFormat("SSS");

    ParseRegions regions;

    Element config;

    File dataDirectory;
    boolean fileExists;
    static Category logger = 
	Category.getInstance(BreqFastAvailableData.class.getName());
    
}// BreqFastEventChannelSubsetter
