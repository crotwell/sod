package edu.sc.seis.sod;

import java.awt.Dimension;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;
import javax.xml.transform.TransformerException;
import org.apache.log4j.Logger;
import org.apache.xpath.XPathAPI;
import org.w3c.dom.DOMException;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import edu.iris.Fissures.Time;
import edu.iris.Fissures.Unit;
import edu.iris.Fissures.model.BoxAreaImpl;
import edu.iris.Fissures.model.GlobalAreaImpl;
import edu.iris.Fissures.model.MicroSecondDate;
import edu.iris.Fissures.model.QuantityImpl;
import edu.iris.Fissures.model.TimeInterval;
import edu.iris.Fissures.model.UnitImpl;
import edu.iris.Fissures.model.UnitRangeImpl;
import edu.sc.seis.fissuresUtil.chooser.ClockUtil;
import edu.sc.seis.fissuresUtil.display.MicroSecondTimeRange;
import edu.sc.seis.fissuresUtil.exceptionHandler.GlobalExceptionHandler;
import edu.sc.seis.fissuresUtil.xml.XMLUtil;
import edu.sc.seis.sod.status.TemplateFileLoader;
import edu.sc.seis.sod.subsetter.LatitudeRange;
import edu.sc.seis.sod.subsetter.LongitudeRange;

public class SodUtil {

    public static boolean isTrue(Element el, String tagName) {
        Element booleanElement = getElement(el, tagName);
        if(booleanElement != null && isTrueText(getNestedText(booleanElement))) { return true; }
        return false;
    }

    public static boolean isTrueText(String nestedText) {
        if(nestedText.equals("TRUE")) { return true; }
        return false;
    }

    public static File makeOutputDirectory(Element config)
            throws ConfigurationException {
        String outputDirName = "html";
        if(config != null) {
            Element outputElement = SodUtil.getElement(config,
                                                       "outputDirectory");
            if(outputElement != null) {
                if(SodUtil.getText(outputElement) != null) {
                    outputDirName = SodUtil.getText(outputElement);
                }
            }
        }
        File htmlDir = new File(outputDirName);
        if(outputDirName != null) htmlDir = new File(outputDirName);
        if(!htmlDir.exists()) htmlDir.mkdirs();
        if(!htmlDir.isDirectory()) { throw new ConfigurationException("The output directory specified in the config file already exists, and isn't a directory"); }
        return htmlDir;
    }

    public static synchronized Object load(Element config, String armName)
            throws ConfigurationException {
        return load(config, new String[] {armName});
    }

    public static synchronized Object load(Element config, String[] armNames)
            throws ConfigurationException {
        try {
            String tagName = config.getTagName();
            // make sure first letter is upper case
            String firstLetter = tagName.substring(0, 1);
            firstLetter = firstLetter.toUpperCase();
            tagName = firstLetter + tagName.substring(1);
            // first check for things that are not SodElements
            if(tagName.equals("Unit")) {
                return loadUnit(config);
            } else if(tagName.equals("UnitRange")) {
                return loadUnitRange(config);
            } else if(tagName.equals("TimeRange")) {
                System.out.println("TIMERANGE!");
                return loadTimeRange(config);
            } else if(tagName.equals("GlobalArea")) {
                return new GlobalAreaImpl();
            } else if(tagName.equals("BoxArea")) {
                return loadBoxArea(config);
            } else if(tagName.equals("PointArea")) { return loadBoxArea(config); }
            // not a known non-sodElement type, so load via reflection
            if(tagName.startsWith("External")) { return loadExternal(tagName,
                                                                     armNames,
                                                                     config); }
            return loadClass(load(tagName, armNames), config);
        } catch(InvocationTargetException e) {
            // occurs if the constructor throws an exception
            // don't repackage ConfigurationException
            Throwable subException = e.getTargetException();
            if(subException instanceof ConfigurationException) {
                throw (ConfigurationException)subException;
            } else if(subException instanceof Exception) {
                throw new ConfigurationException("Problem creating "
                        + config.getTagName(), subException);
            } else {
                // not an Exception, so must be an Error
                throw (java.lang.Error)subException;
            } // end of else
        } catch(Exception e) {
            throw new ConfigurationException("Problem understanding "
                    + config.getTagName(), e);
        } // end of try-catch
    }

    private static Class load(String tagName, String[] armNames)
            throws ClassNotFoundException {
        if(tagName.equals("beginOffset") || tagName.equals("endOffset")) {
            tagName = "interval";
        }
        //Load for each of the arms....
        for(int j = 0; j < armNames.length; j++) {
            String armName = armNames[j];
            for(int i = 0; i < basePackageNames.length; i++) {
                String packageName = baseName + "." + basePackageNames[i] + "."
                        + armName;
                try {
                    return Class.forName(packageName + "." + tagName);
                } catch(ClassNotFoundException ex) {}//will be handled at the
                // end
            }
        }
        //load for the base packages....
        for(int i = 0; i < basePackageNames.length; i++) {
            String packageName = baseName + "." + basePackageNames[i];
            try {
                return Class.forName(packageName + "." + tagName);
            } catch(ClassNotFoundException ex) {}//will be handled at the
            // end
        }
        return Class.forName(baseName + "." + tagName);
    }

    /**
     * loads the class named in the element "classname" in config with config as
     * a costructor argument. If the loaded class doesnt implement
     * mustImplement, a configuration exception is thrown
     */
    public static synchronized Object loadExternal(String tagName,
                                                   String[] armNames,
                                                   Element config)
            throws Exception {
        String classname = getNestedText(SodUtil.getElement(config, "classname"));
        try {
            Class extClass = Class.forName(classname);
            Class mustImplement = load(tagName.substring("external".length()),
                                       armNames);
            Class[] implementedInterfaces = getInterfaces(extClass);
            for(int i = 0; i < implementedInterfaces.length; i++) {
                Class curInterface = implementedInterfaces[i];
                if(curInterface.equals(mustImplement)) { return loadClass(extClass,
                                                                          config); }
            }
            throw new ConfigurationException("External class " + classname
                    + " does not implement the class it's working with, "
                    + mustImplement + ".  Make classname implement "
                    + mustImplement
                    + " to use it at this point in the strategy file.");
        } catch(ClassNotFoundException e1) {
            throw new ConfigurationException("Unable to find external class "
                    + classname + ".  Make sure it's on the classpath", e1);
        }
    }

    private static Class[] getInterfaces(Class c) {
        List l = new ArrayList();
        Class[] cInterfaces = c.getInterfaces();
        for(int i = 0; i < cInterfaces.length; i++) {
            l.add(cInterfaces[i]);
        }
        if(c.getSuperclass() != null) {
            Class[] superInterfaces = getInterfaces(c.getSuperclass());
            for(int i = 0; i < superInterfaces.length; i++) {
                l.add(superInterfaces[i]);
            }
        }
        return (Class[])l.toArray(new Class[l.size()]);
    }

    private static Object loadClass(Class subsetter, Element config)
            throws Exception {
        Class[] argTypes = {Element.class};
        Constructor constructor = null;
        try {
            constructor = subsetter.getConstructor(argTypes);
            Object[] args = {config};
            return constructor.newInstance(args);
        } catch(NoSuchMethodException e) {
            // no constructor with Element, try empty
            argTypes = new Class[0];
            constructor = subsetter.getConstructor(argTypes);
            return constructor.newInstance(new Object[0]);
        }
    }

    private static String baseName = "edu.sc.seis.sod";

    private static String[] basePackageNames = {"subsetter",
                                                "process",
                                                "status"};

    public static UnitImpl loadUnit(Element config)
            throws ConfigurationException {
        String unitName = null;
        NodeList children = config.getChildNodes();
        if(children.item(0) instanceof Text) {
            unitName = children.item(0).getNodeValue();
        }
        try {
            return UnitImpl.getUnitFromString(unitName);
        } catch(NoSuchFieldException e) {
            throw new ConfigurationException("Can't find unit " + unitName, e);
        } // end of try-catch
    }

    public static Time loadTime(Element el) throws ConfigurationException {
        if(el == null) return null;
        edu.iris.Fissures.Time rtnTime = null;
        NodeList kids = el.getChildNodes();
        for(int i = 0; i < kids.getLength(); i++) {
            Node node = kids.item(i);
            if(node instanceof Element) {
                String tagName = ((Element)node).getTagName();
                Element element = (Element)node;
                if(tagName.equals("earlier") || tagName.equals("later")) {
                    rtnTime = loadRelativeTime(element);
                } else if(tagName.equals("now")) {
                    rtnTime = ClockUtil.now().getFissuresTime();
                }
            }
        }
        if(rtnTime == null) {
            String time = getNestedText(el);
            rtnTime = new Time(time, 0);
        }
        return rtnTime;
    }

    private static Time loadRelativeTime(Element el)
            throws ConfigurationException {
        TimeInterval duration = loadTimeInterval(el);
        MicroSecondDate now = ClockUtil.now();
        if(el.getTagName().equals("earlier")) {
            return now.subtract(duration).getFissuresTime();
        } else {
            return now.add(duration).getFissuresTime();
        }
    }

    public static TimeInterval loadTimeInterval(Element config)
            throws ConfigurationException {
        try {
            double value = Double.parseDouble(XMLUtil.getText(XMLUtil.getElement(config,
                                                                                 "value")));
            UnitImpl unit = loadUnit(XMLUtil.getElement(config, "unit"));
            return new TimeInterval(value, unit);
        } catch(Exception e) {
            throw new ConfigurationException("Can't load TimeInterval from "
                    + config.getTagName(), e);
        } // end of try-catch
    }

    public static QuantityImpl loadQuantity(Element config)
            throws ConfigurationException {
        try {
            double value = Double.parseDouble(XMLUtil.getText(XMLUtil.getElement(config,
                                                                                 "value")));
            UnitImpl unit = loadUnit(XMLUtil.getElement(config, "unit"));
            return new QuantityImpl(value, unit);
        } catch(Exception e) {
            throw new ConfigurationException("Can't load quantity from "
                    + config.getTagName(), e);
        } // end of try-catch
    }

    public static void copyFile(String src, String dest)
            throws FileNotFoundException {
        if(src.startsWith("jar")) {
            try {
                URL url = TemplateFileLoader.getUrl(SodUtil.class.getClassLoader(),
                                                    src);
                copyStream(url.openStream(), dest);
            } catch(Exception e) {
                GlobalExceptionHandler.handle("trouble creating url for copying",
                                              e);
            }
        } else {
            File f = new File(src);
            copyStream(new FileInputStream(f), dest);
        }
    }

    public static void copyStream(InputStream src, String dest) {
        File f = new File(dest);
        f.getParentFile().mkdirs();
        OutputStream os = null;
        try {
            os = new BufferedOutputStream(new FileOutputStream(f));
            int curChar;
            while((curChar = src.read()) != -1)
                os.write(curChar);
        } catch(IOException e) {
            GlobalExceptionHandler.handle("Troble copying a file", e);
        } finally {
            if(os != null) {
                try {
                    os.close();
                } catch(IOException e1) {
                    GlobalExceptionHandler.handle("Unable to close output stream for file "
                                                          + f,
                                                  e1);
                }
            }
        }
    }

    public static UnitRangeImpl loadUnitRange(Element config)
            throws ConfigurationException {
        Unit unit = null;
        double min = Double.MIN_VALUE;
        double max = Double.MAX_VALUE;
        NodeList children = config.getChildNodes();
        for(int i = 0; i < children.getLength(); i++) {
            if(children.item(i) instanceof Element) {
                Element subElement = (Element)children.item(i);
                String tagName = subElement.getTagName();
                if(tagName.equals("unit")) {
                    unit = loadUnit(subElement);
                } else if(tagName.equals("min")) {
                    min = Double.parseDouble(getText(subElement));
                } else if(tagName.equals("max")) {
                    max = Double.parseDouble(getText(subElement));
                }
            }
        }
        UnitRangeImpl unitRange = new UnitRangeImpl(min, max, unit);
        return unitRange;
    }

    public static MicroSecondTimeRange loadTimeRange(Element config)
            throws ConfigurationException {
        NodeList children = config.getChildNodes();
        Time begin = null, end = null;
        for(int i = 0; i < children.getLength(); i++) {
            if(children.item(i) instanceof Element) {
                Element subElement = (Element)children.item(i);
                String tagName = subElement.getTagName();
                if(tagName.equals("startTime")) {
                    begin = loadTime(subElement);
                } else if(tagName.equals("endTime")) {
                    end = loadTime(subElement);
                }
            }
        }
        return new MicroSecondTimeRange(begin, end);
    }

    public static Dimension loadDimensions(Element element) throws Exception {
        String width = nodeValueOfXPath(element, "width/text()");
        String height = nodeValueOfXPath(element, "height/text()");
        return new Dimension(Integer.parseInt(width), Integer.parseInt(height));
    }

    public static String nodeValueOfXPath(Element el, String xpath)
            throws DOMException, TransformerException {
        return XPathAPI.selectSingleNode(el, xpath).getNodeValue();
    }

    public static BoxAreaImpl loadBoxArea(Element config)
            throws ConfigurationException {
        NodeList children = config.getChildNodes();
        float minLatitude = 0;
        float maxLatitude = 0;
        float minLongitude = 0;
        float maxLongitude = 0;
        for(int i = 0; i < children.getLength(); i++) {
            if(children.item(i) instanceof Element) {
                Object obj = SodUtil.load((Element)children.item(i), "");
                if(obj instanceof LatitudeRange) {
                    minLatitude = (float)((LatitudeRange)obj).getMinValue();
                    maxLatitude = (float)((LatitudeRange)obj).getMaxValue();
                } else if(obj instanceof LongitudeRange) {
                    minLongitude = (float)((LongitudeRange)obj).getMinValue();
                    maxLongitude = (float)((LongitudeRange)obj).getMaxValue();
                }
            }
        }
        return new BoxAreaImpl(minLatitude,
                               maxLatitude,
                               minLongitude,
                               maxLongitude);
    }

    /**
     * returns the element with the given name
     */
    public static Element getElement(Element config, String elementName) {
        NodeList children = config.getChildNodes();
        for(int counter = 0; counter < children.getLength(); counter++) {
            if(children.item(counter) instanceof Element) {
                Element el = (Element)children.item(counter);
                if(el.getTagName().equals(elementName)) { return el; }
            }
        }
        return null;
    }

    /**
     * returns the first text child within the node.
     */
    public static String getText(Element config) {
        NodeList children = config.getChildNodes();
        for(int i = 0; i < children.getLength(); i++) {
            if(children.item(i) instanceof Text) { return children.item(i)
                    .getNodeValue(); }
        }
        //nothing found, return null
        return null;
    }

    /** returns the nested text in the tag * */
    public static String getNestedText(Element config) {
        //logger.debug("The element name in sod util is "+config.getTagName());
        String rtnValue = null;
        NodeList children = config.getChildNodes();
        Node node;
        //logger.debug("The length of the children is "+children.getLength());
        for(int i = 0; i < children.getLength(); i++) {
            node = children.item(i);
            if(node instanceof Text) {
                //logger.debug("In sodUtil textnode value is
                // "+node.getNodeValue());
                rtnValue = node.getNodeValue();
                //break;
            } else if(node instanceof Element) {
                //logger.debug("in sod util tag name is
                // "+((Element)node).getTagName());
                rtnValue = getNestedText((Element)node);
                break;
            }
        }
        return rtnValue;
    }

    public static String getRelativePath(String fromPath,
                                         String toPath,
                                         String separator) {
        StringTokenizer fromTok = new StringTokenizer(fromPath, separator);
        StringTokenizer toTok = new StringTokenizer(toPath, separator);
        StringBuffer dotBuf = new StringBuffer();
        StringBuffer pathBuf = new StringBuffer();
        while(fromTok.countTokens() > 1 || toTok.countTokens() > 1) {
            if(fromTok.countTokens() > 1 && toTok.countTokens() > 1) {
                String fromTmp = fromTok.nextToken();
                String toTmp = toTok.nextToken();
                if(!fromTmp.equals(toTmp)) {
                    dotBuf.append(".." + separator);
                    pathBuf.append(toTmp + separator);
                }
            } else if(fromTok.countTokens() > 1) {
                fromTok.nextToken();
                dotBuf.append(".." + separator);
            } else if(toTok.countTokens() > 1) {
                pathBuf.append(toTok.nextToken() + separator);
            }
        }
        pathBuf.append(toTok.nextToken());
        return dotBuf.toString() + pathBuf.toString();
    }

    public static String getAbsolutePath(String baseLoc, String relativeLoc)
            throws IOException {
        if(baseLoc.startsWith("jar:")) {
            String noFileBase = removeFileName(baseLoc);
            int numDirUp = countDots(relativeLoc);
            String relBase = stripDirs(noFileBase, numDirUp);
            return relBase + stripRelativeBits(relativeLoc);
        } else {
            String base = new File(baseLoc).getCanonicalFile().getParent();
            int numDirUp = countDots(relativeLoc);
            for(int i = 0; i < numDirUp; i++) {
                base = new File(base).getParent();
            }
            return base + '/' + stripRelativeBits(relativeLoc);
        }
    }

    private static String stripDirs(String base, int numDirUp) {
        for(int i = 0; i < numDirUp; i++) {
            base = base.substring(0, base.lastIndexOf("/"));
        }
        return base + '/';
    }

    private static String removeFileName(String base) {
        return base.substring(0, base.lastIndexOf("/"));
    }

    private static String stripRelativeBits(String relativeLoc) {
        while(relativeLoc.indexOf("../") == 0) {
            relativeLoc = relativeLoc.substring(3);
        }
        if(relativeLoc.indexOf("./") == 0) {
            relativeLoc = relativeLoc.substring(2);
        }
        if(relativeLoc.indexOf("/") == 0) {
            relativeLoc = relativeLoc.substring(1);
        }
        return relativeLoc;
    }

    private static int countDots(String relativeLocation) {
        int lastDots = relativeLocation.lastIndexOf("..");
        if(lastDots == -1) { return 0; }
        return 1 + lastDots / 3;
    }

    public static void loadProperties(Element config, Properties props) {
        NodeList children = config.getChildNodes();
        Node node;
        for(int i = 0; i < children.getLength(); i++) {
            node = children.item(i);
            if(node instanceof Element) {
                if(((Element)node).getTagName().equals("property")) {
                    Element elem = (Element)node;
                    String propName = SodUtil.getNestedText(SodUtil.getElement(elem,
                                                                               "name"));
                    String propValue = SodUtil.getNestedText(SodUtil.getElement(elem,
                                                                                "value"));
                    props.setProperty(propName, propValue);
                }
            }
        }
    }

    public static int[] intArrayFromList(List list) {
        int[] array = new int[list.size()];
        for(int i = 0; i < array.length; i++) {
            array[i] = ((Integer)list.get(i)).intValue();
        }
        return array;
    }

    public static final UnitImpl[] LENGTH_UNITS = {UnitImpl.KILOMETER,
                                                   UnitImpl.METER,
                                                   UnitImpl.CENTIMETER,
                                                   UnitImpl.NANOMETER,
                                                   UnitImpl.MICROMETER,
                                                   UnitImpl.MICRON,
                                                   UnitImpl.MILLIMETER,
                                                   UnitImpl.PICOMETER,
                                                   UnitImpl.INCH,
                                                   UnitImpl.FOOT,
                                                   UnitImpl.MILE,
                                                   UnitImpl.DEGREE,
                                                   UnitImpl.RADIAN};

    public static final UnitImpl[] TIME_UNITS = {UnitImpl.HOUR,
                                                 UnitImpl.NANOSECOND,
                                                 UnitImpl.MICROSECOND,
                                                 UnitImpl.MILLISECOND,
                                                 UnitImpl.SECOND,
                                                 UnitImpl.MINUTE,
                                                 UnitImpl.DAY,
                                                 UnitImpl.WEEK,
                                                 UnitImpl.FORTNIGHT};

    public static final UnitImpl[] FREQ_UNITS = {UnitImpl.HERTZ};

    private static Logger logger = Logger.getLogger(SodUtil.class);
}// SubsetterUtil
