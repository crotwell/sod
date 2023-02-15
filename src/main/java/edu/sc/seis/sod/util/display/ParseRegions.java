package edu.sc.seis.sod.util.display;

import java.io.IOException;
import java.io.InputStream;
import java.text.Collator;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Locale;
import java.util.Properties;

import edu.sc.seis.sod.model.event.FlinnEngdahlRegion;
import edu.sc.seis.sod.model.event.FlinnEngdahlType;


/**
 * ParseRegions.java
 *
 *
 * Created: Fri Sep 28 12:35:36 2001
 *
 */

public class ParseRegions {
    private ParseRegions (){ load(); }

    public static ParseRegions getInstance() {
        if (singleton == null) { singleton = new ParseRegions(); }
        return singleton;
    }

    private static ParseRegions singleton = null;

    /** Gets the Geographic region number for a name. Returns 0 if the
     name cannot be found. */
    public int getRegionValue(String region) {
        for (int i = 0; i < geogRegions.length; i++) {
            if(geogRegions[i].getName().equals(region)){
                return geogRegions[i].getNumber();
            }
        }
        return 0;
    }

    public int getSeismicRegionValue(String region) {
        for (int i = 0; i < seisRegions.length; i++) {
            if(seisRegions[i].getName().equals(region)){
                return seisRegions[i].getNumber();
            }
        }
        return 0;
    }

    public GeographicRegion getGeographicRegion(int geoNum){
        if(geoNum <= NUM_GEOGRAPHIC_REGIONS && geoNum > 0){
            return geogRegions[geoNum - 1];
        }
        return new GeographicRegion(new SeismicRegion("Unknown", 0),"Unknown (" + geoNum+")", geoNum);
    }

    public String getGeographicRegionName(int geoNum) {
        if (geoNum == 0){
            return "Unknown";
        }            
        return getGeographicRegion(geoNum).getName();
    }

    public String getSeismicRegionName(int seisNum) {
        if(seisNum <= NUM_SEISMIC_REGIONS && seisNum > 0){
            return seisRegions[seisNum - 1].getName();
        }
        return "SeisRegion"+seisNum;
    }

    public String getRegionName(FlinnEngdahlRegion region){
        if (region != null && region.type != null) {
            if (region.type.equals(FlinnEngdahlType.SEISMIC_REGION)) {
                return getSeismicRegionName(region.number);
            }
            if (region.type.equals(FlinnEngdahlType.GEOGRAPHIC_REGION)) {
                return getGeographicRegionName(region.number);
            }
        }
        return "Unknown";
    }

    public SeismicRegion[] getAllSeismicRegions(){ return seisRegions; }

    public SeismicRegion[] getAlphabetizedSeismicRegions(){
        SeismicRegion[] alphaRegions = new SeismicRegion[NUM_SEISMIC_REGIONS];
        for (int i = 0; i < NUM_SEISMIC_REGIONS; i++) {
            alphaRegions[i] = seisRegions[i];
        }
        Arrays.sort(alphaRegions, new RegionAlphabetizer());
        return alphaRegions;
    }

    private class RegionAlphabetizer implements Comparator{
        public int compare(Object o1, Object o2) {
            if(o1 instanceof SeismicRegion && o2 instanceof SeismicRegion){
                SeismicRegion seis1 = (SeismicRegion)o1;
                SeismicRegion seis2 = (SeismicRegion)o2;
                return alphaCol.compare(seis1.getName(), seis2.getName());
            }
            return 0;
        }

        private Collator alphaCol = Collator.getInstance(Locale.US);
    }

    protected void load() {
        try {
            ClassLoader loader = getClass().getClassLoader();
            InputStream fstream =
                loader.getResourceAsStream(FE_REGION_PROP);
            if (fstream == null) {
                throw new RuntimeException("Cannot load FE regions from "+FE_REGION_PROP+", stream is null");
            }
            Properties feProps = new Properties();
            feProps.load(fstream);
            for (int i = 1; i < NUM_SEISMIC_REGIONS + 1; i++) {
                String regionName = feProps.getProperty("SeismicRegion"+i);
                seisRegions[i - 1] = new SeismicRegion(regionName, i);
            }
            for (int i = 1; i < NUM_GEOGRAPHIC_REGIONS + 1; i++) {
                String geogName = feProps.getProperty("GeogRegion" + i);
                int seisRegNum = Integer.parseInt(feProps.getProperty("GeoToSeisMap"+i));
                SeismicRegion parent = seisRegions[seisRegNum - 1];
                geogRegions[i - 1] = new GeographicRegion(parent, geogName, i);
                parent.add(geogRegions[i - 1]);
            }
        } catch (IOException e) {
            throw new RuntimeException("Cannot load FE regions from "+FE_REGION_PROP, e);
        } // end of catch
    }
    
    public static final String FE_REGION_PROP = "edu/sc/seis/sod/util/display/FERegions.prop";

    public static final int NUM_SEISMIC_REGIONS = 50;
    public static final int NUM_GEOGRAPHIC_REGIONS = 757;

    private SeismicRegion[] seisRegions = new SeismicRegion[NUM_SEISMIC_REGIONS];
    private GeographicRegion[] geogRegions = new GeographicRegion[NUM_GEOGRAPHIC_REGIONS];
}// parseRegions
