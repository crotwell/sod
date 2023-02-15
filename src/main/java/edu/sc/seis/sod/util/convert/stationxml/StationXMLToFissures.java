package edu.sc.seis.sod.util.convert.stationxml;

import java.time.Duration;
import java.time.Instant;

import edu.sc.seis.seisFile.TimeUtils;
import edu.sc.seis.seisFile.fdsnws.stationxml.FloatType;
import edu.sc.seis.seisFile.fdsnws.stationxml.Unit;
import edu.sc.seis.sod.model.common.QuantityImpl;
import edu.sc.seis.sod.model.common.UnitBase;
import edu.sc.seis.sod.model.common.UnitImpl;
import edu.sc.seis.sod.model.common.UnknownUnit;

public class StationXMLToFissures {


    public static QuantityImpl convertFloatType(FloatType val) throws UnknownUnit {
        return new QuantityImpl(val.getValue(), convertUnit(val.getUnit(), ""));
    }

    public static UnitImpl convertUnit(Unit unit) throws UnknownUnit {
        String unitString;
        if (unit.getName().indexOf(" - ") != -1) {
            unitString = unit.getName().substring(0, unit.getName().indexOf(" - ")).trim();
        } else {
            unitString = unit.getName().trim(); // probably won't work, but might as well
                                     // try
        }
        if (unitString.length() == 0) {
            // no unit, probably means unknown response
            throw new UnknownUnit(unit);
        }
        return convertUnit(unitString, unit.getDescription());
    }

    public static UnitImpl convertUnit(String unitString, String unitDescription) throws UnknownUnit {
        if (unitDescription == null) {
            unitDescription = "";
        }
        unitString = unitString.trim();
        unitDescription = unitDescription.trim();
        if (unitString.equalsIgnoreCase("M") && ! unitDescription.trim().equalsIgnoreCase("minute")) {
            return UnitImpl.METER;
        } else if (unitString.equalsIgnoreCase("M") && unitDescription.trim().equalsIgnoreCase("minute")) {
            return UnitImpl.MINUTE;
        } else if (unitString.equalsIgnoreCase("NM")) {
            return UnitImpl.NANOMETER;
        } else if (unitString.equalsIgnoreCase("M/S")) {
            return UnitImpl.METER_PER_SECOND;
        } else if (unitString.equalsIgnoreCase("NM/S") || unitString.equalsIgnoreCase("NM/SEC")) {
            return UnitImpl.NANOMETER_PER_SECOND;
        } else if (unitString.equalsIgnoreCase("CM/SEC**2")) {
            return UnitImpl.CENTIMETER_PER_SECOND_PER_SECOND;
        } else if (unitString.equalsIgnoreCase("M/S/S")
                || unitString.equalsIgnoreCase("M/S**2")
                || unitString.equalsIgnoreCase("M/(S**2)")
                || unitString.equalsIgnoreCase("M/S**2/ACCELERATION")) {
            return UnitImpl.METER_PER_SECOND_PER_SECOND;
        } else if (unitString.equalsIgnoreCase("PA") || unitString.equalsIgnoreCase("PASSCAL") || unitString.equalsIgnoreCase("PASSCALS")) {
            return UnitImpl.PASCAL;
        } else if (unitString.equalsIgnoreCase("HPA")  || unitString.equalsIgnoreCase("HECTOPASCALS")) {
            return UnitImpl.HECTOPASCAL;
        } else if (unitString.equalsIgnoreCase("KPA") || unitString.equalsIgnoreCase("KILOPASCALS")) {
            return UnitImpl.KILOPASCAL;
        } else if (unitString.equalsIgnoreCase("H/M**2*S")) {
            return UnitImpl.multiply(UnitImpl.SQUARE_METER, UnitImpl.SECOND).inverse("hail intensity in hits per meter squared second");
        } else if (unitString.equalsIgnoreCase("PERCENT") || unitString.equalsIgnoreCase("P") || unitString.equalsIgnoreCase("%")) {
            return new UnitImpl(UnitBase.COUNT, -2, "PERCENT", 1, 1);
        } else if (unitString.equalsIgnoreCase("MBAR")) {
            return UnitImpl.MILLIBAR;
        } else if (unitString.equalsIgnoreCase("C") || unitString.equalsIgnoreCase("TC") || unitString.equalsIgnoreCase("CELSIUS")
                || unitString.equalsIgnoreCase("DEGC")) {
            return UnitImpl.CELSIUS;
        } else if (unitString.equalsIgnoreCase("S") || unitString.equalsIgnoreCase("SEC")) {
            return UnitImpl.SECOND;
        } else if (unitString.equalsIgnoreCase("USEC")) {
            return UnitImpl.MICROSECOND;
        } else if (unitString.equalsIgnoreCase("A") || unitString.equalsIgnoreCase("AMPERES")) {
            return UnitImpl.AMPERE;
        } else if (unitString.equalsIgnoreCase("T")) {
            return UnitImpl.TESLA;
        } else if (unitString.equalsIgnoreCase("NT")) {
            return UnitImpl.multiply(0.000000001, UnitImpl.TESLA, "NANOTESLA");
        } else if (unitString.equalsIgnoreCase("V")
                || unitString.equalsIgnoreCase("VOLTS")
                || unitString.equalsIgnoreCase("VOLT_UNIT")) {
            return UnitImpl.VOLT;
        } else if (unitString.equalsIgnoreCase("MILLIVOLTS")) {
            return UnitImpl.multiply(.001, UnitImpl.VOLT, "MILLIVOLT");
        } else if (unitString.equalsIgnoreCase("V/M")) {
            return UnitImpl.VOLT_PER_METER;
        } else if (unitString.equalsIgnoreCase("W/M2") || unitString.equalsIgnoreCase("WATTS/M^2") ) {
            return UnitImpl.divide(UnitImpl.WATT, UnitImpl.SQUARE_METER);
        } else if (unitString.equalsIgnoreCase("RAD") || 
                unitString.equalsIgnoreCase("RADIAN") || 
                unitString.equalsIgnoreCase("RADIANS") || 
                unitString.equalsIgnoreCase("TILT")) {
            return UnitImpl.RADIAN;
        } else if (unitString.equalsIgnoreCase("MICRORADIANS")) {
            return UnitImpl.multiply(.000001, UnitImpl.RADIAN, "MICRORADIAN");
        } else if (unitString.equalsIgnoreCase("RAD/S")) {
            return UnitImpl.RADIAN_PER_SECOND;
        } else if (unitString.equalsIgnoreCase("MM/HOUR")) {
            return UnitImpl.divide(UnitImpl.MILLIMETER, UnitImpl.HOUR);
        } else if (unitString.equalsIgnoreCase("D") || unitString.equalsIgnoreCase("DEGREES")) {
            return UnitImpl.DEGREE;
        } else if (unitString.equalsIgnoreCase("DEGC")) {
            return UnitImpl.CELSIUS;
        } else if (unitString.equalsIgnoreCase("COUNTS") || unitString.equalsIgnoreCase("COUNT_UNIT")) {
            return UnitImpl.COUNT;
        } else if (unitString.equalsIgnoreCase("REBOOTS")
                || unitString.equalsIgnoreCase("CYCLES")
                || unitString.equalsIgnoreCase("ERROR")
                || unitString.equalsIgnoreCase("BYTES")
                || unitString.equalsIgnoreCase("GAPS")) {
            return UnitImpl.COUNT;
        } else if (unitString.equalsIgnoreCase("B") && unitDescription.trim().equalsIgnoreCase("boolean")) {
            return UnitImpl.DIMENSONLESS;
        } else if (unitString.equalsIgnoreCase("1") || unitString.equalsIgnoreCase("M/M") || unitString.equalsIgnoreCase("NULL")) {
            return UnitImpl.divide(UnitImpl.METER, UnitImpl.METER);
        } else if (unitString.equalsIgnoreCase("M**3/M**3")) {
            return UnitImpl.CUBIC_METER_PER_CUBIC_METER;
        } else if (unitString.equalsIgnoreCase("BITS/SEC")) {
            return UnitImpl.divide(UnitImpl.COUNT, UnitImpl.SECOND);
        } else if (unitString.equalsIgnoreCase("C/S")) {
            return UnitImpl.divide(UnitImpl.COULOMB, UnitImpl.SECOND);
        } else {
            try {
                return UnitImpl.getUnitFromString(unitString);
            } catch(NoSuchFieldException e) {
                throw new UnknownUnit("Unknown unit: '" + unitString + "' described as "+unitDescription);
            }
        }
    }

    public static Instant convertTime(String xml) {
        return  TimeUtils.parseISOString(xml);
    }

    public static Instant convertTime(String xml, String defaultTime) {
        String s = xml;
        if (xml == null) {
            s = defaultTime;
        }
        return TimeUtils.parseISOString(s);
    }

    public static String makeNoNull(String s) {
        if (s == null) {
            return "";
        }
        return s;
    }

    public static final String UNKNOWN = "";

    public static final Duration ONE_SECOND = TimeUtils.ONE_SECOND;
    
    public static final String WAY_FUTURE = "24990101T00:00:00.000";
    
    public static final String WAY_PAST = "10010101T00:00:00.000";

    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(StationXMLToFissures.class);
}
