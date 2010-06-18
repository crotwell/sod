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
import java.util.Calendar;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.transform.TransformerException;

import org.apache.xpath.XPathAPI;
import org.python.core.PyJavaType;
import org.python.core.PyObject;
import org.python.util.PythonInterpreter;
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
import edu.sc.seis.bag.BagUtil;
import edu.sc.seis.fissuresUtil.chooser.ClockUtil;
import edu.sc.seis.fissuresUtil.display.configuration.DOMHelper;
import edu.sc.seis.fissuresUtil.exceptionHandler.GlobalExceptionHandler;
import edu.sc.seis.fissuresUtil.xml.XMLUtil;
import edu.sc.seis.sod.process.waveform.WaveformProcess;
import edu.sc.seis.sod.process.waveform.vector.WaveformVectorProcess;
import edu.sc.seis.sod.source.event.MicroSecondTimeRangeSupplier;
import edu.sc.seis.sod.status.TemplateFileLoader;
import edu.sc.seis.sod.subsetter.LatitudeRange;
import edu.sc.seis.sod.subsetter.LongitudeRange;
import edu.sc.seis.sod.subsetter.availableData.AvailableDataSubsetter;
import edu.sc.seis.sod.subsetter.availableData.vector.VectorAvailableDataSubsetter;
import edu.sc.seis.sod.subsetter.channel.ChannelSubsetter;
import edu.sc.seis.sod.subsetter.dataCenter.SeismogramDCLocator;
import edu.sc.seis.sod.subsetter.eventChannel.EventChannelSubsetter;
import edu.sc.seis.sod.subsetter.eventChannel.vector.EventVectorSubsetter;
import edu.sc.seis.sod.subsetter.eventStation.EventStationSubsetter;
import edu.sc.seis.sod.subsetter.network.NetworkSubsetter;
import edu.sc.seis.sod.subsetter.origin.OriginPointDistance;
import edu.sc.seis.sod.subsetter.origin.OriginSubsetter;
import edu.sc.seis.sod.subsetter.request.RequestSubsetter;
import edu.sc.seis.sod.subsetter.request.vector.VectorRequestSubsetter;
import edu.sc.seis.sod.subsetter.requestGenerator.RandomTimeInterval;
import edu.sc.seis.sod.subsetter.requestGenerator.RequestGenerator;
import edu.sc.seis.sod.subsetter.station.StationSubsetter;

public class SodUtil {

    public static boolean isTrue(Element el, String tagName) {
        Element booleanElement = getElement(el, tagName);
        if(booleanElement != null && isTrueText(getNestedText(booleanElement))) {
            return true;
        }
        return false;
    }

    public static boolean isTrueText(String nestedText) {
        if(nestedText.equals("TRUE")) {
            return true;
        }
        return false;
    }

    public static File makeOutputDirectory(Element config) throws ConfigurationException {
        String outputDirName = "html";
        if(config != null) {
            Element outputElement = SodUtil.getElement(config, "outputDirectory");
            if(outputElement != null) {
                if(SodUtil.getText(outputElement) != null) {
                    outputDirName = SodUtil.getText(outputElement);
                }
            }
        }
        File htmlDir = new File(outputDirName);
        if(outputDirName != null)
            htmlDir = new File(outputDirName);
        if(!htmlDir.exists())
            htmlDir.mkdirs();
        if(!htmlDir.isDirectory()) {
            throw new ConfigurationException("The output directory specified in the config file already exists, and isn't a directory");
        }
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
            tagName = tagName.substring(0, 1).toUpperCase() + tagName.substring(1);
            // Site is no longer separate, so replace site logicals with corresponding channels
            if(tagName.equals("SiteAND")) {
                tagName = "ChannelAND";
            } else if(tagName.equals("SiteOR")) {
                tagName = "ChannelOR";
            } else if(tagName.equals("SiteNOT")) {
                tagName = "ChannelNOT";
            }
            // first check for things that are not SodElements
            if(tagName.equals("Unit")) {
                return loadUnit(config);
            } else if(tagName.equals("UnitRange")) {
                return loadUnitRange(config);
            } else if(tagName.equals("TimeRange")) {
                return loadTimeRange(config);
            } else if(tagName.equals("TimeInterval")) {
                return loadTimeInterval(config);
            } else if(tagName.equals("GlobalArea")) {
                return new GlobalAreaImpl();
            } else if(tagName.equals("BoxArea")) {
                return loadBoxArea(config);
            } else if(tagName.equals("PointDistance")) {
                return new OriginPointDistance(config).getArea();
            }
            // not a known non-sodElement type, so load via reflection
            if(tagName.startsWith("External")) {
                return loadExternal(tagName, armNames, config);
            }
            if(tagName.startsWith("Jython")) {
                return loadJython(tagName, armNames, config);
            }
            return loadClass(load(tagName, armNames), config);
        } catch(InvocationTargetException e) {
            // occurs if the constructor throws an exception
            // don't repackage ConfigurationException
            Throwable subException = e.getTargetException();
            if(subException instanceof ConfigurationException) {
                throw (ConfigurationException)subException;
            } else if(subException instanceof Exception) {
                throw new ConfigurationException("Problem creating " + config.getTagName(),
                                                 subException);
            } else {
                // not an Exception, so must be an Error
                throw (java.lang.Error)subException;
            } // end of else
        }catch(ConfigurationException ce){
            //Just rethrow config exceptions 
            throw ce;
        } catch(Exception e) {
            throw new ConfigurationException("Problem understanding " + config.getTagName(), e);
        } // end of try-catch
    }

    private static Class load(String tagName, String[] armNames) throws ClassNotFoundException {
        if (tagName.equals("siteAND") ||tagName.equals("siteOR") || tagName.equals("siteNOT") || tagName.equals("siteXOR")) {
            logger.warn(tagName +" has been removed and all site subsetters are now channel subsetters. "+
                        "Please up date your recipe to use channelAND, channelOR and channelNOT instead.");
            tagName = "channel"+tagName.substring("site".length());
        }
        if(tagName.equals("beginOffset") || tagName.equals("endOffset")) {
            tagName = "interval";
        }
        // Load for each of the arms....
        for(int j = 0; j < armNames.length; j++) {
            String armName = armNames[j];
            for(int i = 0; i < basePackageNames.length; i++) {
                String packageName = baseName + "." + basePackageNames[i] + "." + armName;
                try {
                    return Class.forName(packageName + "." + tagName);
                } catch(ClassNotFoundException ex) {}// will be handled at
                // the
                // end
            }
        }
        // load for the base packages....
        for(int i = 0; i < basePackageNames.length; i++) {
            String packageName = baseName + "." + basePackageNames[i];
            try {
                return Class.forName(packageName + "." + tagName);
            } catch(ClassNotFoundException ex) {}// will be handled at the
            // end
        }
        return Class.forName(baseName + "." + tagName);
    }

    public static PythonInterpreter getPythonInterpreter() {
        if (interpreter == null) {
            interpreter = new PythonInterpreter();
            interpreter.exec("import sys");
            interpreter.exec("sys.path.append('.')");
            BagUtil.addClassAdapters();
        }
        return interpreter;
    }
    
    public static synchronized Object loadJython(String tagName, String[] armNames, Element config)
    throws Exception {
        Class mustImplement = load(tagName.substring("jython".length()), armNames);
        if (getElement(config, "inline") != null) {
            String jythonCode = getNestedText(getElement(config, "inline"));
            return inlineJython(tagName+pythonClassNum++, mustImplement, jythonCode);
        } else {
            String moduleName = getNestedText(SodUtil.getElement(config, "module"));
            String className = getNestedText(SodUtil.getElement(config, "class"));
            PythonInterpreter interp = getPythonInterpreter();
            interp.exec("from "+moduleName+" import "+className);
            PyObject jyWaveformProcessClass = interp.get(className);
            PyObject pyWaveformProcessObj = jyWaveformProcessClass.__call__( PyJavaType.wrapJavaObject(config));
            return mustImplement.cast(pyWaveformProcessObj.__tojava__(mustImplement));
        }
    }

    protected static synchronized Object inlineJython(String className, Class mustImplement, String jythonCode) throws ClassNotFoundException {
        try {
        
        String[] lines = jythonCode.split("[\\r\\n]+");
        PythonInterpreter interp = getPythonInterpreter();
        interp.setOut(System.out);
        interp.setErr(System.err);
        interp.exec("from "+mustImplement.getPackage().getName()+" import "+mustImplement.getSimpleName());
        interp.exec("from edu.sc.seis.sod.status import Pass, Fail");
        interp.exec("from edu.sc.seis.sod.process.waveform import WaveformResult");
        interp.exec("from edu.iris.Fissures.seismogramDC import LocalSeismogramImpl");
        String classDef = "class "+className+"("+mustImplement.getSimpleName()+"):\n"+
         "  def __init__(self):\n"+
         "      print 'in inline constructor'\n"+
         "      pass\n"+
         "  def accept(self, "+getJythonAcceptArgs(mustImplement)+"):\n"+
         "      print 'in inline accept'\n";
        int numSpaces = 0;
        int firstLine = 0;
        while (lines[firstLine].trim().length() == 0) {
            firstLine++;
        }
        while (lines[firstLine].charAt(numSpaces) == ' ') {
            numSpaces++;
        }
        Pattern pattern = Pattern.compile(" {0,"+numSpaces+"}");
        for (int i = 0; i < lines.length; i++) {
            String trimmedLine = lines[i];
            Matcher matcher = pattern.matcher(trimmedLine);
            if (trimmedLine.length() > 0 && matcher.find()) {
                trimmedLine = trimmedLine.substring(matcher.end());
            }
            classDef += "      "+trimmedLine+"\n";
        }
        interp.exec(classDef);
        PyObject jyWaveformProcessClass = interp.get(className);
        
        PyObject pyWaveformProcessObj = jyWaveformProcessClass.__call__();
        
        Object out = pyWaveformProcessObj.__tojava__(mustImplement);
        return mustImplement.cast(out);
        }catch(RuntimeException e) {
            System.err.println("RuntimeException in SodUtil.inlineJython"+ e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }
    
    protected static String getJythonAcceptArgs(Class mustImplement) {
        if (mustImplement.equals(NetworkSubsetter.class)) {
            return "networkAttr";
        }if (mustImplement.equals(StationSubsetter.class)) {
            return "station, networkSource";
        }if (mustImplement.equals(ChannelSubsetter.class)) {
            return "channel, networkSource";
        }if (mustImplement.equals(OriginSubsetter.class)) {
            return "eventAccess, eventAttr, preferred_origin";
        }if (mustImplement.equals(EventStationSubsetter.class)) {
            return "event, station, cookieJar";
        }
        if (mustImplement.equals(EventChannelSubsetter.class) || mustImplement.equals(RequestGenerator.class)) {
            return "event, channel, cookieJar";
        }
        if (mustImplement.equals(EventVectorSubsetter.class)) {
            return "event, channelGroup, cookieJar";
        }
        if (mustImplement.equals(RequestSubsetter.class)) {
            return "event, channel, request, cookieJar";
        }
        if (mustImplement.equals(VectorRequestSubsetter.class)) {
            return "event, channelGroup, request, cookieJar";
        }
        if (mustImplement.equals(AvailableDataSubsetter.class)) {
            return "event, channel, original, available, cookieJar";
        }
        if (mustImplement.equals(VectorAvailableDataSubsetter.class)) {
            return "event, channelGroup, original, available, cookieJar";
        }
        if (mustImplement.equals(WaveformProcess.class)) {
            return "event, channel, original, available, seismograms, cookieJar";
        }
        if (mustImplement.equals(WaveformVectorProcess.class)) {
            return "event, channelGroup, original, available, seismograms, cookieJar";
        }
        if (mustImplement.equals(SeismogramDCLocator.class)) {
            return "event, channel, infilters, cookieJar";
        }
        // should not happen
        throw new IllegalArgumentException("Class mustImplement: "+mustImplement+" is not recognized.");
    }
    
    /**
     * loads the class named in the element "classname" in config with config as
     * a costructor argument. If the loaded class doesnt implement
     * mustImplement, a configuration exception is thrown
     */
    public static synchronized Object loadExternal(String tagName, String[] armNames, Element config)
            throws Exception {
        String classname = getNestedText(SodUtil.getElement(config, "classname"));
        try {
            Class extClass = Class.forName(classname);
            Class mustImplement = load(tagName.substring("external".length()), armNames);
            if(mustImplement.isAssignableFrom(extClass)) {
                return loadClass(extClass, config);
            }
            throw new ConfigurationException("External class " + classname
                    + " does not implement the class it's working with, " + mustImplement
                    + ".  Make " + classname + " implement " + mustImplement
                    + " to use it at this point in the strategy file.");
        } catch(ClassNotFoundException e1) {
            throw new ConfigurationException("Unable to find external class " + classname
                    + ".  Make sure it's on the classpath", e1);
        }
    }

    private static Object loadClass(Class subsetter, Element config) throws Exception {
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

    private static String[] basePackageNames = {"subsetter", "process", "status", "source"};

    public static UnitImpl loadUnit(Element config) throws ConfigurationException {
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

    public static MicroSecondDateSupplier loadTime(Element el) throws ConfigurationException {
        return loadTime(el, false);
    }

    /*
     * If endOfDay is true, and the hours, minutes and seconds are unspecified
     * by this time element, those fields are set to the end of the day
     */
    public static MicroSecondDateSupplier loadTime(final Element el, boolean endOfDay) throws ConfigurationException {
        NodeList kids = el.getChildNodes();
        for(int i = 0; i < kids.getLength(); i++) {
            Node node = kids.item(i);
            if(node instanceof Element) {
                String tagName = ((Element)node).getTagName();
                Element element = (Element)node;
                if(tagName.equals("earlier") || tagName.equals("later")) {
                    return loadRelativeTime(element);
                } else if(tagName.equals("now")) {
                    return nowSupplier();
                } else if(tagName.equals("future")) {
                    return new MicroSecondDateSupplier() {
                        public MicroSecondDate load() {
                            return ClockUtil.wayFuture();
                        }
                    };
                } else {
                    return loadSplitupTime(el, endOfDay);
                }
            }
        }
        return new MicroSecondDateSupplier() {
            final MicroSecondDate date = new MicroSecondDate(new Time(getNestedText(el).trim(), 0));
            public MicroSecondDate load() {  return date; }
        };
    }
    
    public static MicroSecondDateSupplier nowSupplier() {
        return new MicroSecondDateSupplier() {
            public MicroSecondDate load() { return ClockUtil.now(); }
        };
    }

    private static MicroSecondDateSupplier loadSplitupTime(Element element, boolean ceiling)
            throws ConfigurationException {
        int year = DOMHelper.extractInt(element, "year", -1);
        if(year == -1) {
            throw new ConfigurationException("No year given");
        }
        final Calendar cal = createCalendar(year,
                                      DOMHelper.extractInt(element, "month", -1),
                                      DOMHelper.extractInt(element, "day", -1),
                                      DOMHelper.extractInt(element, "hour", -1),
                                      DOMHelper.extractInt(element, "minute", -1),
                                      DOMHelper.extractInt(element, "second", -1),
                                      ceiling);
        return new MicroSecondDateSupplier() {
            final MicroSecondDate date =  new MicroSecondDate(cal.getTime());
            public MicroSecondDate load() {  return date; }
        };
    }

    /**
     * Creates a calendar in the given year. Year must be specified, but all
     * other fields can be -1 if unknown. If -1, they're either the greatest of
     * least value of the calendar's current state depending on the value of
     * ceiling.
     */
    public static Calendar createCalendar(int year,
                                          int month,
                                          int day,
                                          int hour,
                                          int minute,
                                          int second,
                                          boolean ceiling) {
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        cal.set(Calendar.YEAR, year);
        fillInField(Calendar.MONTH, month - 1, ceiling, cal);
        fillInField(Calendar.DAY_OF_MONTH, day, ceiling, cal);
        fillInField(Calendar.HOUR_OF_DAY, hour, ceiling, cal);
        fillInField(Calendar.MINUTE, minute, ceiling, cal);
        fillInField(Calendar.SECOND, second, ceiling, cal);
        fillInField(Calendar.MILLISECOND, -1, ceiling, cal);
        return cal;
    }

    public static void fillInField(int field, int value, boolean ceiling, Calendar cal) {
        if(value >= 0) {
            cal.set(field, value);
        } else if(ceiling) {
            cal.set(field, cal.getActualMaximum(field));
        } else {
            cal.set(field, cal.getActualMinimum(field));
        }
    }

    private static MicroSecondDateSupplier loadRelativeTime(Element el) throws ConfigurationException {
        final TimeInterval duration = loadTimeInterval(DOMHelper.getElement(el, "timeInterval"));
        if(el.getTagName().equals("earlier")) {
            return getEarlierSupplier(duration);
        }
        return new MicroSecondDateSupplier() {
            public MicroSecondDate load() {return ClockUtil.now().add(duration);}
        };
    }
    
    public static MicroSecondDateSupplier getEarlierSupplier(final TimeInterval duration) {
        return new MicroSecondDateSupplier() {
            public MicroSecondDate load() {return ClockUtil.now().subtract(duration);}
        };
    }
    
    public static MicroSecondDateSupplier getLaterSupplier(final TimeInterval duration) {
        return new MicroSecondDateSupplier() {
        public MicroSecondDate load() {return ClockUtil.now().add(duration);}
        };
    };

    public static TimeInterval loadTimeInterval(Element config) throws ConfigurationException {
        try {
            UnitImpl unit = loadUnit(XMLUtil.getElement(config, "unit"));
            if(DOMHelper.hasElement(config, "randomValue")) {
                Element rvConf = DOMHelper.getElement(config, "randomValue");
                double min = Double.parseDouble(DOMHelper.extractText(rvConf, "min"));
                double max = Double.parseDouble(DOMHelper.extractText(rvConf, "max"));
                return new RandomTimeInterval(min, max, unit);
            } else {
                double value = Double.parseDouble(DOMHelper.extractText(config, "value"));
                return new TimeInterval(value, unit);
            }
        } catch(Exception e) {
            throw new ConfigurationException("Can't load TimeInterval from " + config.getTagName(),
                                             e);
        } // end of try-catch
    }

    public static QuantityImpl loadQuantity(Element config) throws ConfigurationException {
        try {
            double value = Double.parseDouble(XMLUtil.getText(XMLUtil.getElement(config, "value")));
            UnitImpl unit = loadUnit(XMLUtil.getElement(config, "unit"));
            return new QuantityImpl(value, unit);
        } catch(Exception e) {
            throw new ConfigurationException("Can't load quantity from " + config.getTagName(), e);
        } // end of try-catch
    }

    public static int loadInt(Element config, String elementName, int defaultValue) {
        Element child = XMLUtil.getElement(config, elementName);
        if(child != null) {
            return Integer.parseInt(XMLUtil.getText(child));
        } else {
            return defaultValue;
        }
    }

    public static String loadText(Element config, String elementName, String defaultValue) {
        Element child = getElement(config, elementName);
        if(child != null) {
            return getText(child);
        } else {
            return defaultValue;
        }
    }

    public static void copyFile(String src, String dest) throws FileNotFoundException {
        if(src.startsWith("jar")) {
            try {
                URL url = TemplateFileLoader.getUrl(SodUtil.class.getClassLoader(), src);
                copyStream(url.openStream(), dest);
            } catch(Exception e) {
                GlobalExceptionHandler.handle("trouble creating url for copying", e);
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
                    GlobalExceptionHandler.handle("Unable to close output stream for file " + f, e1);
                }
            }
        }
    }

    public static UnitRangeImpl loadUnitRange(Element config) throws ConfigurationException {
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

    public static MicroSecondTimeRangeSupplier loadTimeRange(Element config) throws ConfigurationException {
        NodeList children = config.getChildNodes();
        MicroSecondDateSupplier begin = null, end = null;
        for(int i = 0; i < children.getLength(); i++) {
            if(children.item(i) instanceof Element) {
                Element subElement = (Element)children.item(i);
                String tagName = subElement.getTagName();
                if(tagName.equals("startTime")) {
                    begin = loadTime(subElement);
                } else if(tagName.equals("endTime")) {
                    end = loadTime(subElement, true);
                }
            }
        }
        return new BeginEndTimeRangeSupplier(begin, end);
    }

    public static Dimension loadDimensions(Element element) throws Exception {
        String width = nodeValueOfXPath(element, "width/text()");
        String height = nodeValueOfXPath(element, "height/text()");
        return new Dimension(Integer.parseInt(width), Integer.parseInt(height));
    }

    public static String nodeValueOfXPath(Element el, String xpath) throws DOMException,
            TransformerException {
        return XPathAPI.selectSingleNode(el, xpath).getNodeValue();
    }

    public static BoxAreaImpl loadBoxArea(Element config) throws ConfigurationException {
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
        return new BoxAreaImpl(minLatitude, maxLatitude, minLongitude, maxLongitude);
    }

    /**
     * returns the element with the given name
     */
    public static Element getElement(Element config, String elementName) {
        NodeList children = config.getChildNodes();
        for(int counter = 0; counter < children.getLength(); counter++) {
            if(children.item(counter) instanceof Element) {
                Element el = (Element)children.item(counter);
                if(el.getTagName().equals(elementName)) {
                    return el;
                }
            }
        }
        return null;
    }

    public static Element getFirstEmbeddedElement(Element config) {
        NodeList nl = config.getChildNodes();
        for(int i = 0; i < nl.getLength(); i++) {
            if(nl.item(i) instanceof Element) {
                return (Element)nl.item(i);
            }
        }
        return null;
    }

    /**
     * returns the first text child within the node.
     */
    public static String getText(Element config) {
        if (config == null) { throw new IllegalArgumentException("config cannot be null");}
        NodeList children = config.getChildNodes();
        for(int i = 0; i < children.getLength(); i++) {
            if(children.item(i) instanceof Text) {
                return children.item(i).getNodeValue();
            }
        }
        // nothing found, return null
        return null;
    }

    /** returns the nested text in the tag * */
    public static String getNestedText(Element config) {
        // logger.debug("The element name in sod util is "+config.getTagName());
        String rtnValue = null;
        NodeList children = config.getChildNodes();
        Node node;
        // logger.debug("The length of the children is "+children.getLength());
        for(int i = 0; i < children.getLength(); i++) {
            node = children.item(i);
            if(node instanceof Text) {
                // logger.debug("In sodUtil textnode value is
                // "+node.getNodeValue());
                rtnValue = node.getNodeValue();
                // break;
            } else if(node instanceof Element) {
                // logger.debug("in sod util tag name is
                // "+((Element)node).getTagName());
                rtnValue = getNestedText((Element)node);
                break;
            }
        }
        return rtnValue;
    }

    public static String getRelativePath(String fromPath, String toPath, String separator) {
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
        if(toTok.countTokens() > 0) {
            pathBuf.append(toTok.nextToken());
        }
        return dotBuf.toString() + pathBuf.toString();
    }

    public static String getAbsolutePath(String baseLoc, String relativeLoc) throws IOException {
        if(baseLoc.startsWith("jar:")) {
            String noFileBase = removeFileName(baseLoc);
            int numDirUp = countDots(relativeLoc);
            String relBase = stripDirs(noFileBase, numDirUp);
            return relBase + stripRelativeBits(relativeLoc);
        }
        String base = new File(baseLoc).getCanonicalFile().getParent();
        int numDirUp = countDots(relativeLoc);
        for(int i = 0; i < numDirUp; i++) {
            base = new File(base).getParent();
        }
        return base + '/' + stripRelativeBits(relativeLoc);
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
        if(lastDots == -1) {
            return 0;
        }
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
                    String propName = SodUtil.getNestedText(SodUtil.getElement(elem, "name"));
                    String propValue = SodUtil.getNestedText(SodUtil.getElement(elem, "value"));
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
    
    public static String getSimpleName(Class c){
        return c.getName().substring(c.getName().lastIndexOf(".") + 1);
    }
    
    public static PythonInterpreter interpreter;
    
    public static int pythonClassNum = 1;

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
    
    private static final org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(SodUtil.class);
}// SubsetterUtil
