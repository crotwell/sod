package edu.sc.seis.sod;

import org.w3c.dom.*;
import java.lang.reflect.*;
import edu.iris.Fissures.*;
import edu.iris.Fissures.model.*;

/**
 * SubsetterUtil.java
 *
 *
 * Created: Tue Mar 19 12:00:31 2002
 *
 * @author <a href="mailto:">Philip Crotwell</a>
 * @version
 */

public class SodUtil {
    public SodUtil (){
	
    }
    
    public static Object load(Element config, String packageName) 
	throws ConfigurationException {
	
	try {
	    Class[] constructorArgTypes = new Class[1];
	    constructorArgTypes[0] = Element.class;
	    String tagName = config.getTagName();

	    // make sure first letter is upper case
	    String firstLetter = tagName.substring(0,1);
	    firstLetter = firstLetter.toUpperCase();
	    tagName = firstLetter+tagName.substring(1);

	    // first check for things that are not SodElements
	    if (tagName.equals("Unit")) {
		return loadUnit(config);
	    } else if (tagName.equals("UnitRange")) {
		return loadUnitRange(config);
	    } else if (tagName.equals("TimeRange")) {
		return loadTimeRange(config);
	    } else if (tagName.equals("GlobalArea")) {
		return loadGlobalArea(config);
	    } else if (tagName.equals("BoxArea")) {
		return loadBoxArea(config);
	    } else if (tagName.equals("PointArea")) {
		return loadBoxArea(config);
	    } else if (tagName.equals("FlinnEngdahlArea")) {
		return loadFEArea(config);
	    }
	   
	    // not a known non-sodElement type, so load via reflection
	    Class subsetterSubclass = 
		Class.forName(packageName+"."+
			      tagName);
	    Constructor constructor = 
		subsetterSubclass.getConstructor(constructorArgTypes);
	    Object[] constructorArgs = new Object[1];
	    constructorArgs[0] = config;
	    Object obj = 
		constructor.newInstance(constructorArgs);
	    if(tagName.equals("NetworkFinder")) return obj;
	    return (SodElement)obj;
	} catch (InvocationTargetException e) {
	    // occurs if the constructor throws an exception
	    // don't repackage ConfigurationException
	    Throwable subException = e.getTargetException();
	    if (subException instanceof ConfigurationException) {
		throw (ConfigurationException)subException;
	    } else if (subException instanceof Exception) {
		throw new ConfigurationException("Problem creating "+
						 config.getTagName(),
						 (Exception)subException);
	    } else {
		// not an Exception, so must be an Error
		throw (java.lang.Error)subException;
	    } // end of else
	} catch (Exception e) {
	    e.printStackTrace();
	    throw new ConfigurationException("Problem understanding "+
					     config.getTagName(), e);
	} // end of try-catch
    }

    public static edu.iris.Fissures.model.UnitImpl loadUnit(Element config) throws ConfigurationException {
	String unitName = null;
	NodeList children = config.getChildNodes();
	Node node = children.item(0);
	logger.debug(node.getNodeName());
	if (node instanceof Text) {
	    unitName = node.getNodeValue();
	}
	try {
	    Field field =
		edu.iris.Fissures.model.UnitImpl.class.getField(unitName);
	    return (edu.iris.Fissures.model.UnitImpl)field.get(edu.iris.Fissures.model.UnitImpl.SECOND);
	} catch (Exception e) {
	    throw new ConfigurationException("Can't find unit "+unitName, e);
	} // end of try-catch
    }

    public static edu.iris.Fissures.model.UnitRangeImpl loadUnitRange(Element config)  throws ConfigurationException {
	Unit unit = null;
	double min = Double.MIN_VALUE;
	double max = Double.MAX_VALUE;

	NodeList children = config.getChildNodes();
	Node node;
	for (int i=0; i<children.getLength(); i++) {
	    node = children.item(i);
	    logger.debug(node.getNodeName());
	    if (node instanceof Element) {
		Element subElement = (Element)node;
		String tagName = subElement.getTagName();
		 if (tagName.equals("unit")) {
		    unit = loadUnit(subElement);
		} else if (tagName.equals("min")) {
		    min = Double.parseDouble(getText(subElement));
		} else if (tagName.equals("max")) {
		    max = Double.parseDouble(getText(subElement));
		}		
		 UnitRangeImpl unitRange = new UnitRangeImpl(min, max, unit);
		 return unitRange;
	    } // end of if (node instanceof Element)
	} // end of for (int i=0; i<children.getSize(); i++)
	throw new ConfigurationException("Unable to load unit from "+config.getNodeName());
    }

    public static edu.iris.Fissures.TimeRange loadTimeRange(Element config) 
	throws ConfigurationException {
	NodeList children = config.getChildNodes();
	Node node;
	edu.iris.Fissures.Time begin=null, end=null;
	for (int i=0; i<children.getLength(); i++) {
	    node = children.item(i);
	    logger.debug(node.getNodeName());
	    if (node instanceof Element) {
		Element subElement = (Element)node;
		String tagName = subElement.getTagName();
		 if (tagName.equals("startTime")) {
		     ISOTime tmp = new ISOTime(getText(subElement));
		     begin = tmp.getDate().getFissuresTime();
		} else if (tagName.equals("endTime")) {
		    ISOTime tmp = new ISOTime(getText(subElement));
		    end = tmp.getDate().getFissuresTime();
		}		
	    } // end of if (node instanceof Element)
	} // end of for (int i=0; i<children.getSize(); i++)
	return new TimeRange(begin, end);
    }
    
    public static edu.iris.Fissures.model.GlobalAreaImpl loadGlobalArea(Element config)  throws ConfigurationException {
	return new GlobalAreaImpl();
    }

    public static edu.iris.Fissures.model.BoxAreaImpl loadBoxArea(Element config)  throws ConfigurationException {
	return null;
    }

    public static edu.iris.Fissures.model.PointDistanceAreaImpl loadPointArea(Element config)  throws ConfigurationException {
	return null;
    }

    public static edu.iris.Fissures.model.FlinnEngdahlRegionImpl loadFEArea(Element config)  throws ConfigurationException {
	return null;
    }

    /** returns the first text child within the node.
     */
    public static String getText(Element config) {
	NodeList children = config.getChildNodes();
	Node node;
	for (int i=0; i<children.getLength(); i++) {
	    node = children.item(i);
	    if (node instanceof Text) {
		return node.getNodeValue();
	    }
	}
	//nothing found, return null
	return null;
    }

	/** returns the nested text in the tag **/
	public static String getNestedText(Element config) {
		String rtnValue = null;
		NodeList children = config.getChildNodes();
		Node node;
		for(int i = 0; i < children.getLength(); i++) {
			
			node = children.item(i);
			if (node instanceof Text){ 
				rtnValue =  node.getNodeValue();
				break;
			}
			else rtnValue = getNestedText((Element)node);		
		}		
		return rtnValue;
	}
	
    static org.apache.log4j.Category logger = 
        org.apache.log4j.Category.getInstance(SodUtil.class.getName());

}// SubsetterUtil
