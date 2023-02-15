package edu.sc.seis.sod.bag;

import java.text.DecimalFormatSymbols;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;

import edu.sc.seis.seisFile.TimeUtils;
import edu.sc.seis.seisFile.fdsnws.stationxml.BaseFilterType;
import edu.sc.seis.seisFile.fdsnws.stationxml.Coefficients;
import edu.sc.seis.seisFile.fdsnws.stationxml.InstrumentSensitivity;
import edu.sc.seis.seisFile.fdsnws.stationxml.PolesZeros;
import edu.sc.seis.seisFile.fdsnws.stationxml.Response;
import edu.sc.seis.seisFile.fdsnws.stationxml.ResponseList;
import edu.sc.seis.seisFile.fdsnws.stationxml.ResponseListElement;
import edu.sc.seis.seisFile.fdsnws.stationxml.ResponseStage;
import edu.sc.seis.seisFile.fdsnws.stationxml.StationXMLTagNames;
import edu.sc.seis.seisFile.fdsnws.stationxml.Unit;
import edu.sc.seis.sod.model.common.TimeRange;
import edu.sc.seis.sod.model.common.UnitImpl;
import edu.sc.seis.sod.model.common.UnknownUnit;
import edu.sc.seis.sod.model.station.ChannelId;
import edu.sc.seis.sod.util.convert.stationxml.StationXMLToFissures;

/**
 * ResponsePrint.java
 * 
 * 
 * Created: Wed Mar 7 19:15:59 2001
 * 
 * @author K. Sue Schoch
 * @version
 * 
 * This class prints a channel response in evalresp format.
 */
public class ResponsePrint {

    public ResponsePrint() {}

    /**
     * Prints an response object in resp format. Declared statically so
     * can just be called with edu.iris.dmc.Client.printResponse( ChannelId,
     * Instrumentation );
     * 
     * @param chanId
     *            channel id for the response
     * @param response
     *            object containing the entire response
     * @param effective_time
     *            time range for this response, usually the channel effective time
     */
    public static String printResponse(ChannelId chanId,
                                       Response response,
                                       TimeRange effective_time) {
        Instant stime = effective_time.getBeginTime();
        Instant etime = effective_time.getEndTime();
        InstrumentSensitivity sensitivity = response.getInstrumentSensitivity();
        StringBuffer r = new StringBuffer();
        r.append("\n#");
        r.append("\n###################################################################################");
        r.append("\n#");
        r.append("\nB050F03     Station:       " + chanId.getStationCode());
        r.append("\nB050F16     Network:       "
                + chanId.getNetworkId());
        r.append("\nB052F03     Location:      " + chanId.getLocCode());
        r.append("\nB052F04     Channel:       " + chanId.getChannelCode());
        r.append("\nB052F22     Start date:    " + sdf.format(stime));
        r.append("\nB052F23     End date:      " + sdf.format(etime));
        for(int i = 0; i < response.getResponseStageList().size(); i++) {
            int stageNum = i + 1;
            ResponseStage stage = response.getResponseStageList().get(i);
            r.append(printB53(chanId, effective_time, stageNum, stage));
            r.append(printB54(chanId, effective_time, stageNum, stage));
            r.append(printB55(chanId, effective_time, stageNum, stage));
            r.append(printB57(chanId, effective_time, stageNum, stage));
            r.append(printB58(chanId, effective_time, stageNum, stage));
        }
        r.append(printSensitivity(chanId, sensitivity, effective_time));
        // System.out.println( r.toString() );
        return r.toString();
    }

    /**
     * Used to print a channel ID and the effective times.
     * 
     * @param id
     *            channel id for the response
     * @param effective_time
     *            effective time of the channel
     */
    public static String printHeader(ChannelId id, TimeRange effective_time) {
        StringBuffer s = new StringBuffer("");
        s.append("#                   |        " + id.getNetworkId()
                + "  ");
        if(id.getStationCode().length() == 5)
            s.append(id.getStationCode() + " ");
        if(id.getStationCode().length() == 4)
            s.append(id.getStationCode() + "  ");
        if(id.getStationCode().length() == 3)
            s.append(id.getStationCode() + "   ");
        if(id.getStationCode().length() == 2)
            s.append(id.getStationCode() + "    ");
        if(id.getLocCode().length() == 1)
            s.append(id.getLocCode() + "  ");
        if(id.getLocCode().length() == 2)
            s.append(id.getLocCode() + " ");
        s.append(id.getChannelCode() + "           |\n");
        Instant stime = effective_time.getBeginTime();
        Instant etime = effective_time.getEndTime();
        s.append("#                   |     " + mdyFormat.format(stime));
        s.append(" to " + mdyFormat.format(etime) + "      |\n");
        s.append("#                   +-----------------------------------+\n");
        s.append("#\n");
        return s.toString();
    }

    /**
     * Prints poles and zeros response blockette 53
     * 
     * @param id
     *            channel id for the response
     * @param effective_time
     *            effective time of the channel
     * @param stageNum
     *            the stage number
     * @param stage
     *            the entire stage object
     */
    public static String printB53(ChannelId id,
                                  TimeRange effective_time,
                                  int stageNum,
                                  ResponseStage stage) {
        // Not a poles and zeros filter
        BaseFilterType filter = stage.getResponseItem();
        if( ! (filter instanceof PolesZeros))
            return "";
        PolesZeros pz = (PolesZeros)filter;
        String transferType = "";
        if(pz.getPzTransferType().equals(StationXMLTagNames.POLEZERO_LAPLACE_RAD_PER_SEC))
            transferType = "A";
        else if(pz.getPzTransferType().equals(StationXMLTagNames.POLEZERO_LAPLACE_HERTZ))
            transferType = "B";
        else if(pz.getPzTransferType().equals(StationXMLTagNames.POLEZERO_DIGITAL))
            transferType = "D";
        java.text.DecimalFormat f = new java.text.DecimalFormat("+0.00000E00;-0.00000E00", new DecimalFormatSymbols(Locale.US));
        java.text.DecimalFormat ind = new java.text.DecimalFormat(" 0", new DecimalFormatSymbols(Locale.US));
        StringBuffer s = new StringBuffer("\n#");
        s.append("\n#                   +-----------------------------------+");
        s.append("\n#                   |     Response (Poles and Zeros)    |\n");
        s.append(printHeader(id, effective_time));
        s.append("B053F03     Transfer function type:            "
                + transferType);
        s.append("\nB053F04     Stage sequence number:             " + stageNum);
        s.append("\nB053F05     Response in units lookup:          "
                + formatUnit(pz.getInputUnits()));
        s.append("\nB053F06     Response out units lookup:         "
                + formatUnit(pz.getOutputUnits()));
        // careful here as normalization is optional in stages,
        // and may be a zero length array
        s.append("\nB053F07     AO normalization factor:           ");
        s.append(f.format(pz.getNormalizationFactor()));
        s.append("\nB053F08     Normalization frequency:           ");
            s.append(f.format(pz.getNormalizationFreq()));
        s.append("\nB053F09     Number of zeroes:                  "
                + pz.getZeroList().size());
        s.append("\nB053F14     Number of poles:                   "
                + pz.getPoleList().size());
        if(pz.getZeroList().size() > 0) {
            s.append("\n#              Complex zeroes:");
            s.append("\n#               i  real          imag          real_error    imag_error");
            for(int k = 0; k < pz.getZeroList().size(); k++) {
                if(k < 10)
                    s.append("\nB053F10-13      " + k + "  "
                            + f.format(pz.getZeroList().get(k).getReal()) + "  "
                            + f.format(pz.getZeroList().get(k).getImaginary()) + "  "
                            + f.format(pz.getZeroList().get(k).getRealWithError().getPlusError()) + "  "
                            + f.format(pz.getZeroList().get(k).getImaginaryWithError().getPlusError()));
                else
                    s.append("\nB053F10-13     " + k + "  "
                            + f.format(pz.getZeroList().get(k).getReal()) + "  "
                            + f.format(pz.getZeroList().get(k).getImaginary()) + "  "
                            + f.format(pz.getZeroList().get(k).getRealWithError().getPlusError()) + "  "
                            + f.format(pz.getZeroList().get(k).getImaginaryWithError().getPlusError()));
            }
        }
        if(pz.getPoleList().size() > 0) {
            s.append("\n#              Complex poles:");
            s.append("\n#               i  real          imag          real_error    imag_error");
            for(int k = 0; k < pz.getPoleList().size(); k++) {
                if(k < 10)
                    s.append("\nB053F15-18      " + k + "  "
                            + f.format(pz.getPoleList().get(k).getReal()) + "  "
                            + f.format(pz.getPoleList().get(k).getImaginary()) + "  "
                            + f.format(pz.getPoleList().get(k).getRealWithError().getPlusError()) + "  "
                            + f.format(pz.getPoleList().get(k).getImaginaryWithError().getPlusError()));
                else
                    s.append("\nB053F15-18     " + k + "  "
                            + f.format(pz.getPoleList().get(k).getReal()) + "  "
                            + f.format(pz.getPoleList().get(k).getImaginary()) + "  "
                            + f.format(pz.getPoleList().get(k).getRealWithError().getPlusError()) + "  "
                            + f.format(pz.getPoleList().get(k).getImaginaryWithError().getPlusError()));
            }
        }
        s.append("\n#");
        return s.toString();
    }

    /**
     * Prints Coefficient responses blockette 53
     * 
     * @param id
     *            channel id for the response
     * @param effective_time
     *            effective time of the channel
     * @param stageNum
     *            the stage number
     * @param stage
     *            the entire stage object
     */
    public static String printB54(ChannelId id,
                                  TimeRange effective_time,
                                  int stageNum,
                                  ResponseStage stage) {
        // Check if a coefficients stage
        BaseFilterType filter = stage.getResponseItem();
        if( ! (filter instanceof Coefficients))
            return "";
        Coefficients c = (Coefficients)filter;
        StringBuffer s = new StringBuffer("\n#");
        String transferType = "";
        if(c.getCfTransferType().equals(StationXMLTagNames.COEFFICIENT_ANALOG_RAD_PER_SEC))
            transferType = "A";
        else if(c.getCfTransferType().equals(StationXMLTagNames.COEFFICIENT_ANALOG_HERTZ))
            transferType = "B";
        else if(c.getCfTransferType().equals(StationXMLTagNames.COEFFICIENT_DIGITAL))
            transferType = "D";
        s.append("\n#                   +-----------------------------------+");
        s.append("\n#                   |       Response (Coefficients)     |\n");
        s.append(printHeader(id, effective_time));
        s.append("B054F03     Transfer function type:            "
                + transferType);
        s.append("\nB054F04     Stage sequence number:             " + stageNum);
        s.append("\nB054F05     Response in units lookup:          "
                + formatUnit(c.getInputUnits()));
        s.append("\nB054F06     Response out units lookup:         "
                + formatUnit(c.getOutputUnits()));
        s.append("\nB054F07     Number of numerators:              "
                + c.getNumeratorList().size());
        s.append("\nB054F10     Number of denominators:            "
                + c.getDenominatorList().size());
        s.append("\n#");
        java.text.DecimalFormat f = new java.text.DecimalFormat("+0.00000E00;-0.00000E00", new DecimalFormatSymbols(Locale.US));
        if(c.getNumeratorList().size() > 0) {
            s.append("\n#              Numerator coefficients:");
            s.append("\n#               i  coefficient   error");
            for(int k = 0; k < c.getNumeratorList().size(); k++) {
                if(k < 10)
                    s.append("\nB054F08-09      " + k + "  "
                            + f.format(c.getNumeratorList().get(k).getValue()) + "  "
                            + f.format(c.getNumeratorList().get(k).getPlusError()));
                else
                    s.append("\nB054F08-09     " + k + "  "
                            + f.format(c.getNumeratorList().get(k).getValue()) + "  "
                            + f.format(c.getNumeratorList().get(k).getPlusError()));
            }
        }
        if(c.getDenominatorList().size() > 0) {
            s.append("\n#              Denominator coefficients:");
            s.append("\n#               i  coefficient   error");
            for(int k = 0; k < c.getDenominatorList().size(); k++) {
                if(k < 10)
                    s.append("\nB054F11-12      " + k + "  "
                            + f.format(c.getDenominatorList().get(k).getValue()) + "  "
                            + f.format(c.getDenominatorList().get(k).getPlusError()));
                else
                    s.append("\nB054F11-12     " + k + "  "
                            + f.format(c.getDenominatorList().get(k).getValue()) + "  "
                            + f.format(c.getDenominatorList().get(k).getPlusError()));
            }
        }
        s.append("\n#");
        return s.toString();
    }

    /**
     * Prints list response SEED blockette 55
     * 
     * @param id
     *            channel id for the response
     * @param effective_time
     *            effective time of the channel
     * @param stageNum
     *            the stage number
     * @param stage
     *            the entire stage object
     */
    public static String printB55(ChannelId id,
                                  TimeRange effective_time,
                                  int stageNum,
                                  ResponseStage stage) {
        // Check if a list stage
        BaseFilterType filter = stage.getResponseItem();
        if( ! (filter instanceof ResponseList))
            return "";
        ResponseList c = (ResponseList)filter;
        StringBuffer s = new StringBuffer("\n#");
        s.append("\n#                   +-----------------------------------+");
        s.append("\n#                   |       Response List               |\n");
        s.append(printHeader(id, effective_time));
        s.append("\nB055F03     Stage sequence number:             " + stageNum);
        s.append("\nB055F04     Response in units lookup:          "
                + formatUnit(c.getInputUnits()));
        s.append("\nB055F05     Response out units lookup:         "
                + formatUnit(c.getOutputUnits()));
        s.append("\nB055F06     Number of responses listed:        "
                + c.getResponseElements().size());
        s.append("\n#");
        java.text.DecimalFormat f = new java.text.DecimalFormat("+0.00000E00;-0.00000E00", new DecimalFormatSymbols(Locale.US));
        java.text.DecimalFormat ind = new java.text.DecimalFormat(" 0", new DecimalFormatSymbols(Locale.US));
        if(c.getResponseElements().size() > 0) {
            s.append("\n#              i  frequency     amplitude     amplitude err phase angle   phase err");
            for(int k = 0; k < c.getResponseElements().size(); k++) {
                ResponseListElement rle = c.getResponseElements().get(k);
                if(k < 10) {
                    s.append("\nB055F07-11      " + k);
                } else {
                    s.append("\nB055F07-11     " + k);
                }
                s.append("  " + f.format(rle.getFrequency()));
                s.append("  " + f.format(rle.getAmplitude().getValue()));
                s.append("  " + f.format(rle.getAmplitude().getPlusError()));
                s.append("  " + f.format(rle.getPhase().getValue()));
                s.append("  " + f.format(rle.getPhase().getPlusError()));
            }
        }
        s.append("\n#");
        return s.toString();
    }

    /**
     * Prints deciamation response SEED blockette 57
     * 
     * @param id
     *            channel id for the response
     * @param effective_time
     *            effective time of the channel
     * @param stageNum
     *            the stage number
     * @param stage
     *            the entire stage object
     */
    public static String printB57(ChannelId id,
                                  TimeRange effective_time,
                                  int stageNum,
                                  ResponseStage stage) {
        if(stage.getDecimation() == null)
            return "";
        java.text.DecimalFormat f = new java.text.DecimalFormat("+0.0000E00;-0.0000E00", new DecimalFormatSymbols(Locale.US));
        StringBuffer s = new StringBuffer("#\n");
        s.append("#                   +-----------------------------------+\n");
        s.append("#                   |             Decimation            |\n");
        s.append(printHeader(id, effective_time));
        s.append("B057F03     Stage sequence number:             " + stageNum);
        s.append("\nB057F04     Input sample rate (HZ):            "
                + f.format(stage.getDecimation().getInputSampleRate()));
        s.append("\nB057F05     Decimation factor:                 "
                + stage.getDecimation().getFactor());
        s.append("\nB057F06     Decimation offset:                 "
                + stage.getDecimation().getOffset());
        s.append("\nB057F07     Estimated delay (seconds):         "
                + f.format(stage.getDecimation().getDelay()));
        s.append("\nB057F08     Correction applied (seconds):      "
                + f.format(stage.getDecimation().getCorrection()));
        s.append("\n#");
        return s.toString();
    }

    /**
     * Prints sensitivity/gain response SEED blockette 58
     * 
     * @param id
     *            channel id for the response
     * @param effective_time
     *            effective time of the channel
     * @param stageNum
     *            the stage number
     * @param stage
     *            the entire stage object
     */
    public static String printB58(ChannelId id,
                                  TimeRange effective_time,
                                  int stageNum,
                                  ResponseStage stage) {
        java.text.DecimalFormat f = new java.text.DecimalFormat("+0.00000E00;-0.00000E00", new DecimalFormatSymbols(Locale.US));
        StringBuffer s = new StringBuffer("#\n");
        s.append("#                   +-----------------------------------+\n");
        s.append("#                   |      Channel Sensitivity/Gain     |\n");
        s.append(printHeader(id, effective_time));
        s.append("B058F03     Stage sequence number:             " + stageNum);
        s.append("\nB058F04     Sensitivity:                       "
                + f.format(stage.getStageSensitivity().getSensitivityValue()));
        s.append("\nB058F05     Frequency of sensitivity:          "
                + f.format(stage.getStageSensitivity().getFrequency()));
        s.append("\nB058F06     Number of calibrations:            0\n");
        return s.toString();
    }

    /**
     * Prints the overall sensitivity/gain response SEED blockette 58
     * 
     * @param id
     *            channel id for the response
     * @param response
     *            the entire instrument response
     */
    public static String printSensitivity(ChannelId id, Response response, TimeRange effectiveTime) {
        return printSensitivity(id,
                                response.getInstrumentSensitivity(),
                                effectiveTime);
    }

    public static String printSensitivity(ChannelId id,
                                          InstrumentSensitivity sensitivity,
                                          TimeRange effective_time) {
        StringBuffer s = new StringBuffer("#\n");
        s.append("#                   +-----------------------------------+\n");
        s.append("#                   |      Channel Sensitivity/Gain     |\n");
        s.append(printHeader(id, effective_time));
        s.append("#\n");
        s.append("B058F03     Stage sequence number:             0");
        s.append("\nB058F04     Sensitivity:                       "
                + sensitivity.getSensitivityValue());
        s.append("\nB058F05     Frequency of sensitivity:          "
                + sensitivity.getFrequency());
        s.append("\nB058F06     Number of calibrations:            0\n");
        return s.toString();
    }

    public static String formatUnit(Unit unit) {
        if(unit == null) {
            return "null";
        }
        try {
        UnitImpl impl = StationXMLToFissures.convertUnit(unit);
        Iterator it = unitNames.keySet().iterator();
        while(it.hasNext()) {
            UnitImpl key = (UnitImpl)it.next();
            if (impl.equals(key)) {
                return (String)unitNames.get(key);
            }
        }
        // no configured name, auto-generate
        return unit.toString();
        } catch (UnknownUnit e) {
            // oh well
            return unit.getName();
        }
    }
    
    public static void addToNameMap(UnitImpl unit, String name) {
        unitNames.put(unit, name);
    }
    
    private static HashMap unitNames = new HashMap();
    
    static {
        addToNameMap(UnitImpl.VOLT, "V - Volts");
        addToNameMap(UnitImpl.COUNT, "COUNTS");
        addToNameMap(UnitImpl.METER_PER_SECOND, "M/S - Velocity in Meters/Second");
        addToNameMap(UnitImpl.NANOMETER, "NM");
        addToNameMap(UnitImpl.MILLIMETER, "MM");
        addToNameMap(UnitImpl.CENTIMETER, "CM");
        addToNameMap(UnitImpl.METER, "M");
        addToNameMap(UnitImpl.NANOMETER_PER_SECOND, "NM/S");
        addToNameMap(UnitImpl.MILLIMETER_PER_SECOND, "MM/S");
        addToNameMap(UnitImpl.CENTIMETER_PER_SECOND, "CM/S");
        addToNameMap(UnitImpl.NANOMETER_PER_SECOND_PER_SECOND, "NM/S**2");
        addToNameMap(UnitImpl.MILLIMETER_PER_SECOND_PER_SECOND, "MM/S**2");
        addToNameMap(UnitImpl.CENTIMETER_PER_SECOND_PER_SECOND, "CM/S**2");
        addToNameMap(UnitImpl.METER_PER_SECOND_PER_SECOND, "M/S**2");
        addToNameMap(UnitImpl.PASCAL, "P");
        addToNameMap(UnitImpl.TESLA, "T");
    }
    
    static DateTimeFormatter mdyFormat = TimeUtils.createFormatter("MM/dd/yyyy");

    static DateTimeFormatter sdf = TimeUtils.createFormatter("yyyy,DDD,HH:mm:ss");
}
