package edu.sc.seis.sod;
import edu.iris.Fissures.model.*;
import java.io.*;

import edu.iris.Fissures.TimeRange;
import edu.iris.Fissures.Unit;
import edu.sc.seis.fissuresUtil.exceptionHandler.GlobalExceptionHandler;
import edu.sc.seis.fissuresUtil.xml.XMLUtil;
import edu.sc.seis.sod.status.TemplateFileLoader;
import edu.sc.seis.sod.subsetter.LatitudeRange;
import edu.sc.seis.sod.subsetter.LongitudeRange;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.Properties;
import java.util.StringTokenizer;
import org.apache.log4j.Logger;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

public class SodUtil {

    public static synchronized Object loadExternal(Element config)
        throws ConfigurationException {
        try {
            String classname;
            Element classNameElement = SodUtil.getElement(config, "classname");
            classname = getNestedText(classNameElement);
            Class[] argTypes = { Element.class };
            Class extClass = Class.forName(classname);
            Constructor constructor = extClass.getConstructor(argTypes);
            Object[] args = {config};
            Object obj = constructor.newInstance(args);
            return (SodElement)obj;
        } catch (InvocationTargetException e) {
            // occurs if the constructor throws an exception
            // don't repackage ConfigurationExceptioN
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
            throw new ConfigurationException("Problem understanding "+
                                                 config.getTagName(), e);
        } // end of try-catch

    }

    public static File makeOutputDirectory(Element config) throws ConfigurationException{
        String outputDirName = "html";
        if(config != null){
            Element outputElement = SodUtil.getElement(config, "outputDirectory");
            if(outputElement != null){
                if(SodUtil.getText(outputElement) != null){
                    outputDirName = SodUtil.getText(outputElement);
                }
            }
        }
        File htmlDir = new File(outputDirName);
        if(outputDirName != null) htmlDir = new File(outputDirName);
        if(!htmlDir.exists()) htmlDir.mkdirs();
        if(!htmlDir.isDirectory()){
            throw new ConfigurationException("The output directory specified in the config file already exists, and isn't a directory");
        }
        return htmlDir;
    }

    public static synchronized Object load(Element config, String armName)
        throws ConfigurationException {
        try {
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
            //Load for each of the arms....
            for (int i = 0; i < basePackageNames.length; i++) {
                String packageName = baseName + "." + basePackageNames[i] + "." + armName;
                try {
                    return loadClass(packageName + "."+ tagName, config);
                } catch (ClassNotFoundException ex) {}//will be handled at the end
            }
            //load for the base packages....
            for (int i = 0; i < basePackageNames.length; i++) {
                String packageName = baseName + "." + basePackageNames[i];
                try {
                    return loadClass(packageName + "."+ tagName, config);
                } catch (ClassNotFoundException ex) {}//will be handled at the end
            }
            return loadClass(baseName + "." + tagName, config);
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
            throw new ConfigurationException("Problem understanding "+
                                                 config.getTagName(), e);
        } // end of try-catch
    }

    private static Object loadClass(String name, Element config)throws Exception{
        Class[] argTypes = {Element.class};
        Class subsetter = Class.forName(name);
        Constructor constructor = subsetter.getConstructor(argTypes);
        Object[] args = {config };
        return constructor.newInstance(args);
    }

    private static String baseName = "edu.sc.seis.sod";

    private static String[] basePackageNames = { "subsetter", "process",
            "status" };

    public static UnitImpl loadUnit(Element config) throws ConfigurationException {
        String unitName = null;
        NodeList children = config.getChildNodes();
        Node node = children.item(0);
        if (node instanceof Text) {
            unitName = node.getNodeValue();
        }

        try {
            return UnitImpl.getUnitFromString(unitName);
        } catch (NoSuchFieldException e) {
            throw new ConfigurationException("Can't find unit "+unitName, e);
        } // end of try-catch
    }


    public static TimeInterval loadTimeInterval(Element config) throws ConfigurationException {
        try {
            double value = Double.parseDouble(XMLUtil.getText(XMLUtil.getElement(config, "value")));
            UnitImpl unit = loadUnit(XMLUtil.getElement(config, "unit"));
            return new TimeInterval(value, unit);
        } catch (Exception e) {
            throw new ConfigurationException("Can't load TimeInterval from "+config.getTagName(), e);
        } // end of try-catch
    }

    public static QuantityImpl loadQuantity(Element config) throws ConfigurationException {
        try {
            double value = Double.parseDouble(XMLUtil.getText(XMLUtil.getElement(config, "value")));
            UnitImpl unit = loadUnit(XMLUtil.getElement(config, "unit"));
            return new QuantityImpl(value, unit);
        } catch (Exception e) {
            throw new ConfigurationException("Can't load quantity from "+config.getTagName(), e);
        } // end of try-catch
    }

    public static void copyFile(String src, String dest) throws FileNotFoundException {
        if(src.startsWith("jar")){
            try {
                URL url = TemplateFileLoader.getUrl(SodUtil.class.getClassLoader(), src);
                copyStream(url.openStream(), dest);
            } catch (Exception e) {
                GlobalExceptionHandler.handle("trouble creating url for copying", e);
            }
        }else{
            File f = new File(src);
            copyStream(new FileInputStream(f), dest);
        }
    }

    public static void copyStream(InputStream src, String dest){
        File f = new File(dest);
        f.getParentFile().mkdirs();
        try {
            FileOutputStream fos =  new FileOutputStream(f);
            int curChar;
            while((curChar = src.read()) != -1) fos.write(curChar);
        } catch (IOException e) {
            GlobalExceptionHandler.handle("Troble copying a file", e);
        }
    }

    public static edu.iris.Fissures.model.UnitRangeImpl loadUnitRange(Element config)  throws ConfigurationException {
        Unit unit = null;
        double min = Double.MIN_VALUE;
        double max = Double.MAX_VALUE;

        NodeList children = config.getChildNodes();
        Node node;
        for (int i=0; i < children.getLength(); i++) {
            node = children.item(i);
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

            } // end of if (node instanceof Element)
        } // end of for (int i=0; i<children.getSize(); i++)
        UnitRangeImpl unitRange = new UnitRangeImpl(min, max, unit);
        return unitRange;
    }

    public static edu.iris.Fissures.TimeRange loadTimeRange(Element config)
        throws ConfigurationException {
        NodeList children = config.getChildNodes();
        Node node;
        edu.iris.Fissures.Time begin=null, end=null;
        for (int i=0; i<children.getLength(); i++) {
            node = children.item(i);
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
        NodeList children = config.getChildNodes();
        Node node;
        float minLatitude = 0;
        float maxLatitude = 0 ;
        float minLongitude = 0 ;
        float maxLongitude = 0;
        for(int i = 0; i < children.getLength(); i++) {
            node = children.item(i);
            if(node instanceof Element) {
                Object obj = SodUtil.load((Element)node, "");
                if(obj instanceof LatitudeRange) {
                    minLatitude = ((LatitudeRange)obj).getMinValue();
                    maxLatitude = ((LatitudeRange)obj).getMaxValue();
                } else if(obj instanceof LongitudeRange) {
                    minLongitude = ((LongitudeRange)obj).getMinValue();
                    maxLongitude = ((LongitudeRange)obj).getMaxValue();
                }
            }

        }
        return new BoxAreaImpl(minLatitude, maxLatitude, minLongitude, maxLongitude);
    }

    public static edu.iris.Fissures.model.PointDistanceAreaImpl loadPointArea(Element config)  throws ConfigurationException {
        return null;
    }

    public static edu.iris.Fissures.model.FlinnEngdahlRegionImpl loadFEArea(Element config)  throws ConfigurationException {
        return null;
    }

    /** returns the element with the given name
     */

    public static Element getElement(Element config, String elementName) {

        NodeList children = config.getChildNodes();
        Node node;

        for(int counter = 0; counter < children.getLength(); counter++ ) {

            node = children.item(counter);
            if(node instanceof Element ) {

                if(((Element)node).getTagName().equals(elementName)) {
                    //logger.debug("in sodUtil getElement, the element name is "+((Element)node).getTagName());
                    return ((Element)node);
                }
            }

        }
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
        //logger.debug("The element name in sod util is "+config.getTagName());
        String rtnValue = null;
        NodeList children = config.getChildNodes();
        Node node;

        //logger.debug("The length of the children is "+children.getLength());
        for(int i = 0; i < children.getLength(); i++) {

            node = children.item(i);
            if (node instanceof Text){
                //logger.debug("In sodUtil textnode value is  "+node.getNodeValue());
                rtnValue =  node.getNodeValue();
                //break;
            }
            else if(node instanceof Element) {
                //logger.debug("in sod util tag name is "+((Element)node).getTagName());
                rtnValue = getNestedText((Element)node);
                break;
            }
        }
        return rtnValue;
    }

    public static String getRelativePath(String fromPath, String toPath, String separator){
        StringTokenizer fromTok = new StringTokenizer(fromPath, separator);
        StringTokenizer toTok = new StringTokenizer(toPath, separator);
        StringBuffer dotBuf = new StringBuffer();
        StringBuffer pathBuf = new StringBuffer();

        while (fromTok.countTokens() > 1 || toTok.countTokens() > 1){
            if (fromTok.countTokens() > 1 && toTok.countTokens() > 1){
                String fromTmp = fromTok.nextToken();
                String toTmp = toTok.nextToken();
                if (!fromTmp.equals(toTmp)){
                    dotBuf.append(".." + separator);
                    pathBuf.append(toTmp + separator);
                }
            }
            else if (fromTok.countTokens() > 1){
                fromTok.nextToken();
                dotBuf.append(".." + separator);
            }
            else if (toTok.countTokens() > 1){
                pathBuf.append(toTok.nextToken() + separator);
            }
        }

        pathBuf.append(toTok.nextToken());
        return dotBuf.toString() + pathBuf.toString();
    }

    public static void loadProperties(Element config, Properties props) {
        NodeList children = config.getChildNodes();
        Node node;
        for(int i = 0; i < children.getLength(); i++) {
            node = children.item(i);
            if(node instanceof Element) {
                if(((Element)node).getTagName().equals("property")) {
                    Element elem = (Element)node;
                    String propName = SodUtil.getNestedText(SodUtil.getElement(elem, "name"));
                    String propValue = SodUtil.getNestedText(SodUtil.getElement(elem, "value"));
                    props.setProperty(propName, propValue);
                }
            }
        }
    }
    private static Logger logger = Logger.getLogger(SodUtil.class);

}// SubsetterUtil
