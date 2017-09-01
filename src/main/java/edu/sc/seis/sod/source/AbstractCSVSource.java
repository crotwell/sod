package edu.sc.seis.sod.source;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;

import org.w3c.dom.Element;

import com.csvreader.CsvReader;

import edu.sc.seis.seisFile.TimeUtils;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.UserConfigurationException;
import edu.sc.seis.sod.model.common.UnitImpl;
import edu.sc.seis.sod.model.common.UnsupportedFormat;


public abstract class AbstractCSVSource extends AbstractSource {

    public AbstractCSVSource(Element config, String defaultName) {
        super(config, defaultName);
    }

    public AbstractCSVSource(String name) {
        super(name);
    }

    public static final String LONGITUDE = "longitude";
    public static final String LATITUDE = "latitude";
    public static final String ELEVATION = "elevation";
    public static final String DEPTH = "depth";
    public static final String NAME = "name";
    public static final String FE_SEIS_REGION = "flinnEngdahlSeismicRegion";

    public abstract String[] getFields();
    
    public boolean isValidField(String field) {
        String[] fields = getFields();
        for (int i = 0; i < fields.length; i++) {
            if (field.equals(fields[i])) {
                return true;
            }
        }
        return false;
    }
    
    public String concatenateValidFields() {
        String[] fields = getFields();
        String allFields = "";
        for (int i = 0; i < fields.length - 1; i++) {
            allFields += fields[i] + ", ";
        }
        return allFields + fields[fields.length - 1];
    }

    public static final String FE_GEO_REGION = "flinnEngdahlGeographicRegion";
    public static final String FE_REGION = "flinnEngdahlRegion";
    public static final String FE_REGION_TYPE = "flinnEngdahlRegionType";
    public static final String DEPTH_UNITS = "depthUnits";
    public static final String ELEVATION_UNITS = "elevationUnits";
    public static final String UNKNOWN = "unknown";
    public static final Instant DEFAULT_TIME = TimeUtils.parseISOString("1970-01-01T00:00:00Z");
    public static final Instant DEFAULT_END = TimeUtils.future;

    public List<String> validateHeaders(CsvReader csvReader) throws IOException, FileNotFoundException,
            ConfigurationException {
                csvReader.readHeaders();
                List<String> headers = Arrays.asList(csvReader.getHeaders());
                for (int i = 0; i < headers.size(); i++) {
                    String cur = (String)headers.get(i);
                    if (!isValidField(cur)) {
                        throw new UserConfigurationException(cur + " is not a known CSV field.  " + concatenateValidFields()
                                + " are valid options.");
                    }
                }
                return headers;
            }

    public static float loadFloat(List<String> headers, CsvReader csvReader, String headerName, float defaultValue)
            throws UserConfigurationException, IOException {
                if (headers.contains(headerName)) {
                    try {
                        return Float.parseFloat(csvReader.get(headerName));
                    } catch (NumberFormatException e) {
                        throw new UserConfigurationException(csvReader.get(headerName) + " in record " + csvReader.getCurrentRecord()
                                                             + ", column "+headerName+ " is not a valid float.");
                    }
                }
                return defaultValue;
            }

    public static double loadDouble(List<String> headers, CsvReader csvReader, String headerName, double defaultValue)
            throws UserConfigurationException, IOException {
                if (headers.contains(headerName)) {
                    try {
                        return Double.parseDouble(csvReader.get(headerName));
                    } catch (NumberFormatException e) {
                        throw new UserConfigurationException(csvReader.get(headerName) + " in record " + csvReader.getCurrentRecord()
                                                             + ", column "+headerName+ " is not a valid double.");
                    }
                }
                return defaultValue;
            }

    public static String loadString(List<String> headers, CsvReader csvReader, String headerName, String defaultValue)
            throws UserConfigurationException, IOException {
                if (headers.contains(headerName)) {
                    return csvReader.get(headerName);
                }
                return defaultValue;
            }

    public static UnitImpl loadUnit(List<String> headers, CsvReader csvReader, String headerName, UnitImpl defaultUnit)
            throws UserConfigurationException, IOException {
                if (headers.contains(headerName)) {
                    String unitName = csvReader.get(headerName);
                    try {
                        return UnitImpl.getUnitFromString(unitName);
                    } catch(NoSuchFieldException e) {
                        throw new UserConfigurationException(unitName + " in record " + csvReader.getCurrentRecord()
                                                             + ", column "+headerName+ " is not a valid unit name.  Try KILOMETER or METER");
                    }
                }
                return defaultUnit;
            }

    public static Instant loadTime(List<String> headers, CsvReader csvReader, String headerName, Instant defaultTime)
            throws UserConfigurationException, IOException {
                if(headers.contains(headerName)) {
                    Instant time;
                    try {
                        time = TimeUtils.parseISOString(csvReader.get(headerName));
                    } catch(UnsupportedFormat uf) {
                        throw new UserConfigurationException("The time '"
                                                             + csvReader.get(headerName) + "' in record "
                                                             + csvReader.getCurrentRecord() + ", column "+headerName+" is invalid.");
                    }
                }
                return defaultTime;
            }

    protected String csvFilename;}
