package edu.sc.seis.sod.hibernate;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.msgpack.core.MessageBufferPacker;
import org.msgpack.core.MessagePack;
import org.msgpack.core.MessageUnpacker;
import org.omg.CORBA_2_3.ORB;

import edu.sc.seis.seisFile.fdsnws.stationxml.BaseFilterType;
import edu.sc.seis.seisFile.fdsnws.stationxml.Channel;
import edu.sc.seis.seisFile.fdsnws.stationxml.Coefficients;
import edu.sc.seis.seisFile.fdsnws.stationxml.Decimation;
import edu.sc.seis.seisFile.fdsnws.stationxml.FIR;
import edu.sc.seis.seisFile.fdsnws.stationxml.FloatNoUnitType;
import edu.sc.seis.seisFile.fdsnws.stationxml.FloatType;
import edu.sc.seis.seisFile.fdsnws.stationxml.GainSensitivity;
import edu.sc.seis.seisFile.fdsnws.stationxml.InstrumentPolynomial;
import edu.sc.seis.seisFile.fdsnws.stationxml.InstrumentSensitivity;
import edu.sc.seis.seisFile.fdsnws.stationxml.Pole;
import edu.sc.seis.seisFile.fdsnws.stationxml.PoleZero;
import edu.sc.seis.seisFile.fdsnws.stationxml.PolesZeros;
import edu.sc.seis.seisFile.fdsnws.stationxml.Polynomial;
import edu.sc.seis.seisFile.fdsnws.stationxml.Response;
import edu.sc.seis.seisFile.fdsnws.stationxml.ResponseList;
import edu.sc.seis.seisFile.fdsnws.stationxml.ResponseListElement;
import edu.sc.seis.seisFile.fdsnws.stationxml.ResponseStage;
import edu.sc.seis.seisFile.fdsnws.stationxml.StationXMLTagNames;
import edu.sc.seis.seisFile.fdsnws.stationxml.Unit;
import edu.sc.seis.seisFile.fdsnws.stationxml.Zero;
import edu.sc.seis.sod.model.station.Instrumentation;

public class InstrumentationBlob {

    /** for hibernate. */
    protected InstrumentationBlob() {}

    public InstrumentationBlob(Channel chan, Instrumentation inst) {
    
    }

        public InstrumentationBlob(Channel chan, Response response) {
        this.chan = chan;
        this.response = response;
    }

    public static byte[] getResponseAsBlob(Channel chan, Response response) throws IOException {
        MessageBufferPacker packer = MessagePack.newDefaultBufferPacker();
        int mapSize = 0;
        if (response.getResponseStageList() != null) {
            mapSize++;
        }
        if (response.getInstrumentSensitivity() != null) {
            mapSize++;
        }
        packer.packMapHeader(mapSize);
        if (response.getResponseStageList() != null) {
            List<ResponseStage> stageList = response.getResponseStageList();
            packer.packString(StationXMLTagNames.RESPONSESTAGE);
            packer.packArrayHeader(stageList.size());
            for (ResponseStage stage : stageList) {
                packStage(packer, stage);
            }
        }
        if (response.getInstrumentSensitivity() != null) {
            packer.packString(StationXMLTagNames.INSTRUMENT_SENSITIVITY);
        }
        packer.close();
        return packer.toByteArray();
    }

    protected static MessageBufferPacker packStage(MessageBufferPacker packer, ResponseStage stage) throws IOException {
        int objSize = 2;// required: number, gain
        if (stage.getResponseItem() != null) {
            objSize++;
        }
        if (stage.getDecimation() != null) {
            objSize++;
        }
        packer.packMapHeader(objSize);
        packer.packString(StationXMLTagNames.NUMBER).packFloat(stage.getNumber());
        BaseFilterType respItem = stage.getResponseItem();
        if (respItem instanceof PolesZeros) {
            packer.packString(StationXMLTagNames.POLESZEROS);
            packPolesZeros(packer, (PolesZeros)respItem);
        } else if (respItem instanceof Coefficients) {
            packer.packString(StationXMLTagNames.COEFFICIENTS);
            packCoefficients(packer, (Coefficients)respItem);
        } else if (respItem instanceof ResponseList) {
            packer.packString(StationXMLTagNames.RESPONSELIST);
            packResponseList(packer, (ResponseList)respItem);
        } else if (respItem instanceof FIR) {
            packer.packString(StationXMLTagNames.FIR);
            packFIR(packer, (FIR)respItem);
        } else if (respItem instanceof Polynomial) {
            packer.packString(StationXMLTagNames.POLYNOMIAL);
            packPolynomial(packer, (Polynomial)respItem);
        }
        if (stage.getStageSensitivity() != null) {
            packer.packString(StationXMLTagNames.STAGESENSITIVITY);
            packGainSensitivity(packer, stage.getStageSensitivity());
        }
        if (stage.getDecimation() != null) {
            packer.packString(StationXMLTagNames.DECIMATION);
            packDecimation(packer, stage.getDecimation());
        }
        return packer;
    }

    protected static ResponseStage unpackStage(MessageUnpacker unpacker) throws IOException {

        Integer number = 0;
        String resourceId = null;
        BaseFilterType responseItem = null;
        Decimation decimation = null;
        GainSensitivity stageGain = null;
        int objSize = unpacker.unpackMapHeader();
        for (int i = 0; i < objSize; i++) {
            String key = unpacker.unpackString();
            if (StationXMLTagNames.RESOURCEID.equals(key)) {
                resourceId = unpacker.unpackString();
            } else if (StationXMLTagNames.NUMBER.equals(key)) {
                number = unpacker.unpackInt();
            } else if (StationXMLTagNames.POLESZEROS.equals(key)) {
                responseItem = unpackPolesZeros(unpacker);
            } else if (StationXMLTagNames.COEFFICIENTS.equals(key)) {
                responseItem = unpackCoefficients(unpacker);
            } else if (StationXMLTagNames.RESPONSELIST.equals(key)) {
                responseItem = unpackResponseList(unpacker);
            } else if (StationXMLTagNames.FIR.equals(key)) {
                responseItem = unpackFIR(unpacker);
            } else if (StationXMLTagNames.POLYNOMIAL.equals(key)) {
                responseItem = unpackPolynomial(unpacker);
            } else if (StationXMLTagNames.DECIMATION.equals(key)) {
                decimation = unpackDecimation(unpacker);
            } else if (StationXMLTagNames.STAGEGAIN.equals(key)) {
                stageGain = unpackGainSensitivity(unpacker);
            } else {
                throw new IOException("unknown key in object: " + key);
            }
        }
        return new ResponseStage(number,
                              resourceId,
                              responseItem,
                              decimation,
                              stageGain);
    }

    protected static MessageBufferPacker packPolesZeros(MessageBufferPacker packer, PolesZeros polesZeros)
            throws IOException {
        int objSize = 9;// required: all but description
        if (polesZeros.getDescription() != null) {objSize++;}
        packer.packMapHeader(objSize);
        packer.packString(StationXMLTagNames.RESOURCEID).packString(polesZeros.getResourceId());
        packer.packString(StationXMLTagNames.NAME).packString(polesZeros.getName());
        if (polesZeros.getDescription() != null) {
            packer.packString(StationXMLTagNames.DESCRIPTION).packString(polesZeros.getDescription());
        }
        packer.packString(StationXMLTagNames.INPUTUNITS);
        packUnit(packer, polesZeros.getInputUnits());
        packer.packString(StationXMLTagNames.OUTPUTUNITS);
        packUnit(packer, polesZeros.getOutputUnits());
        packer.packString(StationXMLTagNames.PZTRANSFERTYPE).packString(polesZeros.getPzTransferType());
        packer.packString(StationXMLTagNames.NORMALIZATIONFACTOR).packFloat(polesZeros.getNormalizationFactor());
        packer.packString(StationXMLTagNames.NORMALIZATIONFREQ).packFloat(polesZeros.getNormalizationFreq());
        packer.packString(StationXMLTagNames.ZERO).packArrayHeader(polesZeros.getZeroList().size());      
        for (Zero z : polesZeros.getZeroList()) {
            packPoleZero(packer, z);
        }
        packer.packString(StationXMLTagNames.POLE).packArrayHeader(polesZeros.getPoleList().size());      
        for (Pole z : polesZeros.getPoleList()) {
            packPoleZero(packer, z);
        }
        return packer;
    }

    protected static PolesZeros unpackPolesZeros(MessageUnpacker unpacker) throws IOException {
        String resourceId = null;
        String description = null;
        String name = null;
        Unit inputUnits = null;
        Unit outputUnits = null;
        String pzTransferFunctionType = null;
        float normalizationFactor = 1.0f;
        float normalizationFrequency = Float.NaN;
        List<Zero> zeroList = new ArrayList<Zero>();
        List<Pole> poleList = new ArrayList<Pole>();
        int objSize = unpacker.unpackMapHeader();
        for (int i = 0; i < objSize; i++) {
            String key = unpacker.unpackString();
            if (StationXMLTagNames.RESOURCEID.equals(key)) {
                resourceId = unpacker.unpackString();
            } else if (StationXMLTagNames.NAME.equals(key)) {
                name = unpacker.unpackString();
            } else if (StationXMLTagNames.DESCRIPTION.equals(key)) {
                description = unpacker.unpackString();
            } else if (StationXMLTagNames.INPUTUNITS.equals(key)) {
                inputUnits = unpackUnit(unpacker);
            } else if (StationXMLTagNames.OUTPUTUNITS.equals(key)) {
                outputUnits = unpackUnit(unpacker);
            } else if (StationXMLTagNames.NORMALIZATIONFACTOR.equals(key)) {
                normalizationFactor = unpacker.unpackFloat();
            } else if (StationXMLTagNames.NORMALIZATIONFREQ.equals(key)) {
                normalizationFrequency = unpacker.unpackFloat();
            } else if (StationXMLTagNames.POLE.equals(key)) {
                int size = unpacker.unpackArrayHeader();
                for (int j = 0; j < size; j++) {
                    poleList.add((Pole)unpackPoleZero(unpacker, StationXMLTagNames.POLE));
                }
            } else if (StationXMLTagNames.ZERO.equals(key)) {
                int size = unpacker.unpackArrayHeader();
                for (int j = 0; j < size; j++) {
                    zeroList.add((Zero)unpackPoleZero(unpacker, StationXMLTagNames.ZERO));
                }
            } else {
                throw new IOException("unknown key in object: " + key);
            }
        }
        return new PolesZeros(resourceId,
                              name,
                              description,
                              inputUnits,
                              outputUnits,
                              pzTransferFunctionType,
                              normalizationFactor,
                              normalizationFrequency,
                              zeroList,
                              poleList);
    }


    protected static MessageBufferPacker packPoleZero(MessageBufferPacker packer, PoleZero poleZero)
            throws IOException {
        int objSize = 2;// required: realErrored, imaginaryErrored
        packer.packMapHeader(objSize);
        packer.packString(StationXMLTagNames.REAL);
        packFloatNoUnitType(packer, poleZero.getRealWithError());
        packer.packString(StationXMLTagNames.IMAGINARY);
        packFloatNoUnitType(packer, poleZero.getImaginaryWithError());
        return packer;
    }
    
    protected static PoleZero unpackPoleZero(MessageUnpacker unpacker, String tagName) throws IOException {
        FloatNoUnitType realWithError = null;
        FloatNoUnitType imaginaryWithError = null;
        int objSize = unpacker.unpackMapHeader();
        for (int i = 0; i < objSize; i++) {
            String key = unpacker.unpackString();
            if (StationXMLTagNames.REAL.equals(key)) {
                realWithError = unpackFloatNoUnitType(unpacker);
            } else if (StationXMLTagNames.IMAGINARY.equals(key)) {
                imaginaryWithError = unpackFloatNoUnitType(unpacker);
            } else {
                throw new IOException("unknown key in object: " + key);
            }
        }
        if (tagName.equals(StationXMLTagNames.POLE)) {
            return new Pole(realWithError, imaginaryWithError);
        } else {
            return new Zero(realWithError, imaginaryWithError);
        }
    }

    protected static MessageBufferPacker packCoefficients(MessageBufferPacker packer, Coefficients coefficients)
            throws IOException {
        int objSize = 6;// required: all but description
        if (coefficients.getDescription() != null) {objSize++;}
        packer.packMapHeader(objSize);
        packer.packString(StationXMLTagNames.RESOURCEID).packString(coefficients.getResourceId());
        packer.packString(StationXMLTagNames.NAME).packString(coefficients.getName());
        if (coefficients.getDescription() != null) {
            packer.packString(StationXMLTagNames.DESCRIPTION).packString(coefficients.getDescription());
        }
        packer.packString(StationXMLTagNames.INPUTUNITS);
        packUnit(packer, coefficients.getInputUnits());
        packer.packString(StationXMLTagNames.OUTPUTUNITS);
        packUnit(packer, coefficients.getOutputUnits());
        packer.packString(StationXMLTagNames.CFTRANSFERTYPE).packString(coefficients.getCfTransferType());
        packer.packString(StationXMLTagNames.NUMERATOR).packArrayHeader(coefficients.getNumeratorList().size());      
        for (FloatType f : coefficients.getNumeratorList()) {
            packFloatType(packer, f);
        }
        packer.packString(StationXMLTagNames.DENOMINATOR).packArrayHeader(coefficients.getDenominatorList().size());      
        for (FloatType f : coefficients.getDenominatorList()) {
            packFloatType(packer, f);
        }
        return packer;
    }

    protected static Coefficients unpackCoefficients(MessageUnpacker unpacker) throws IOException {
        String resourceId = null;
        String description = null;
        String name = null;
        Unit inputUnits = null;
        Unit outputUnits = null;
        String cfTransferType = null;
        List<FloatType> numeratorList = new ArrayList<FloatType>();
        List<FloatType> denomenatorList = new ArrayList<FloatType>();
        int objSize = unpacker.unpackMapHeader();
        for (int i = 0; i < objSize; i++) {
            String key = unpacker.unpackString();
            if (StationXMLTagNames.RESOURCEID.equals(key)) {
                resourceId = unpacker.unpackString();
            } else if (StationXMLTagNames.NAME.equals(key)) {
                name = unpacker.unpackString();
            } else if (StationXMLTagNames.DESCRIPTION.equals(key)) {
                description = unpacker.unpackString();
            } else if (StationXMLTagNames.INPUTUNITS.equals(key)) {
                inputUnits = unpackUnit(unpacker);
            } else if (StationXMLTagNames.OUTPUTUNITS.equals(key)) {
                outputUnits = unpackUnit(unpacker);
            } else if (StationXMLTagNames.CFTRANSFERTYPE.equals(key)) {
                cfTransferType = unpacker.unpackString();
            } else if (StationXMLTagNames.NUMERATOR.equals(key)) {
                int size = unpacker.unpackArrayHeader();
                for (int j = 0; j < size; j++) {
                    numeratorList.add(unpackFloatType(unpacker));
                }
            } else if (StationXMLTagNames.DENOMINATOR.equals(key)) {
                int size = unpacker.unpackArrayHeader();
                for (int j = 0; j < size; j++) {
                    denomenatorList.add(unpackFloatType(unpacker));
                }
            } else {
                throw new IOException("unknown key in object: " + key);
            }
        }
        return new Coefficients(resourceId, 
                                name, 
                                description, 
                                inputUnits, 
                                outputUnits, 
                                cfTransferType,
                                numeratorList,
                                denomenatorList);
    }

    protected static MessageBufferPacker packResponseList(MessageBufferPacker packer, ResponseList responseList)
            throws IOException {
        int objSize = 5;// required: all but description
        if (responseList.getDescription() != null) {objSize++;}
        packer.packMapHeader(objSize);
        packer.packString(StationXMLTagNames.RESOURCEID).packString(responseList.getResourceId());
        packer.packString(StationXMLTagNames.NAME).packString(responseList.getName());
        if (responseList.getDescription() != null) {
            packer.packString(StationXMLTagNames.DESCRIPTION).packString(responseList.getDescription());
        }
        packer.packString(StationXMLTagNames.INPUTUNITS);
        packUnit(packer, responseList.getInputUnits());
        packer.packString(StationXMLTagNames.OUTPUTUNITS);
        packUnit(packer, responseList.getOutputUnits());
        packer.packString(StationXMLTagNames.RESPONSELISTELEMENT).packArrayHeader(responseList.getResponseElements().size());      
        for (ResponseListElement f : responseList.getResponseElements()) {
            packResponseListElement(packer, f);
        }
        return packer;
    }

    protected static ResponseList unpackResponseList(MessageUnpacker unpacker) throws IOException {
        String resourceId = null;
        String description = null;
        String name = null;
        Unit inputUnits = null;
        Unit outputUnits = null;
        List<ResponseListElement> responseElements = new ArrayList<ResponseListElement>();
        int objSize = unpacker.unpackMapHeader();
        for (int i = 0; i < objSize; i++) {
            String key = unpacker.unpackString();
            if (StationXMLTagNames.RESOURCEID.equals(key)) {
                resourceId = unpacker.unpackString();
            } else if (StationXMLTagNames.NAME.equals(key)) {
                name = unpacker.unpackString();
            } else if (StationXMLTagNames.DESCRIPTION.equals(key)) {
                description = unpacker.unpackString();
            } else if (StationXMLTagNames.INPUTUNITS.equals(key)) {
                inputUnits = unpackUnit(unpacker);
            } else if (StationXMLTagNames.OUTPUTUNITS.equals(key)) {
                outputUnits = unpackUnit(unpacker);
            } else if (StationXMLTagNames.RESPONSELISTELEMENT.equals(key)) {
                int size = unpacker.unpackArrayHeader();
                for (int j = 0; j < size; j++) {
                    responseElements.add(unpackResponseListElement(unpacker));
                }
            } else {
                throw new IOException("unknown key in object: " + key);
            }
        }
        return new ResponseList(resourceId, 
                                name, 
                                description, 
                                inputUnits, 
                                outputUnits, 
                                responseElements);
    }

    protected static MessageBufferPacker packResponseListElement(MessageBufferPacker packer, ResponseListElement rle)
            throws IOException {
        int objSize = 3;// required: all 
        packer.packMapHeader(objSize);
        packer.packString(StationXMLTagNames.FREQUENCY);
        packFloatType(packer, rle.getFrequency());
        packer.packString(StationXMLTagNames.AMPLITUDE);
        packFloatType(packer, rle.getAmplitude());
        packer.packString(StationXMLTagNames.PHASE);
        packFloatType(packer, rle.getPhase());
        return packer;
    }
    protected static ResponseListElement unpackResponseListElement(MessageUnpacker unpacker) throws IOException {
        FloatType frequency = null;
        FloatType amplitude = null;
        FloatType phase = null;
        int objSize = unpacker.unpackMapHeader();
        for (int i = 0; i < objSize; i++) {
            String key = unpacker.unpackString();
            if (StationXMLTagNames.FREQUENCY.equals(key)) {
                frequency = unpackFloatType(unpacker);
            } else if (StationXMLTagNames.AMPLITUDE.equals(key)) {
                amplitude = unpackFloatType(unpacker);
            } else if (StationXMLTagNames.PHASE.equals(key)) {
                phase = unpackFloatType(unpacker);
            } else {
                throw new IOException("unknown key in object: " + key);
            }
        }
        return new ResponseListElement(frequency, amplitude, phase);
    }

    protected static MessageBufferPacker packPolynomial(MessageBufferPacker packer, Polynomial polynomial)
            throws IOException {
        int objSize = 11;// required: all but description
        if (polynomial.getDescription() != null) {objSize++;}
        packer.packMapHeader(objSize);
        packer.packString(StationXMLTagNames.RESOURCEID).packString(polynomial.getResourceId());
        packer.packString(StationXMLTagNames.NAME).packString(polynomial.getName());
        if (polynomial.getDescription() != null) {
            packer.packString(StationXMLTagNames.DESCRIPTION).packString(polynomial.getDescription());
        }
        packer.packString(StationXMLTagNames.INPUTUNITS);
        packUnit(packer, polynomial.getInputUnits());
        packer.packString(StationXMLTagNames.OUTPUTUNITS);
        packUnit(packer, polynomial.getOutputUnits());
        packer.packString(StationXMLTagNames.APPROXIMATIONTYPE).packString(polynomial.getApproximationType());
        packer.packString(StationXMLTagNames.FREQLOWERBOUND);
        packFloatType(packer, polynomial.getFreqLowerBound());
        packer.packString(StationXMLTagNames.FREQUPPERBOUND);
        packFloatType(packer, polynomial.getFreqUpperBound());
        packer.packString(StationXMLTagNames.APPROXLOWERBOUND).packFloat(polynomial.getApproxLowerBound());
        packer.packString(StationXMLTagNames.APPROXUPPERBOUND).packFloat(polynomial.getApproxUpperBound());
        packer.packString(StationXMLTagNames.MAXERROR).packFloat(polynomial.getMaxError());
        packer.packString(StationXMLTagNames.COEFFICIENT).packArrayHeader(polynomial.getCoefficientList().size());      
        for (FloatNoUnitType f : polynomial.getCoefficientList()) {
            packFloatNoUnitType(packer, f);
        }
        return packer;
    }

    protected static Polynomial unpackPolynomial(MessageUnpacker unpacker) throws IOException {
        String resourceId = null;
        String description = null;
        String name = null;
        Unit inputUnits = null;
        Unit outputUnits = null;

        String approximationType = null;
        FloatType freqLowerBound = null;
        FloatType freqUpperBound = null;
        float approxLowerBound = Float.NaN;
        float approxUpperBound = Float.NaN;
        float maxError = Float.NaN;
        List<FloatNoUnitType> coefficientList = new ArrayList<FloatNoUnitType>();
        int objSize = unpacker.unpackMapHeader();
        for (int i = 0; i < objSize; i++) {
            String key = unpacker.unpackString();
            if (StationXMLTagNames.RESOURCEID.equals(key)) {
                resourceId = unpacker.unpackString();
            } else if (StationXMLTagNames.NAME.equals(key)) {
                name = unpacker.unpackString();
            } else if (StationXMLTagNames.DESCRIPTION.equals(key)) {
                description = unpacker.unpackString();
            } else if (StationXMLTagNames.INPUTUNITS.equals(key)) {
                inputUnits = unpackUnit(unpacker);
            } else if (StationXMLTagNames.OUTPUTUNITS.equals(key)) {
                outputUnits = unpackUnit(unpacker);
            } else if (StationXMLTagNames.APPROXIMATIONTYPE.equals(key)) {
                approximationType = unpacker.unpackString();
            } else if (StationXMLTagNames.FREQLOWERBOUND.equals(key)) {
                freqLowerBound = unpackFloatType(unpacker);
            } else if (StationXMLTagNames.FREQUPPERBOUND.equals(key)) {
                freqUpperBound = unpackFloatType(unpacker);
            } else if (StationXMLTagNames.APPROXLOWERBOUND.equals(key)) {
                approxLowerBound = unpacker.unpackFloat();
            } else if (StationXMLTagNames.APPROXUPPERBOUND.equals(key)) {
                approxUpperBound = unpacker.unpackFloat();
            } else if (StationXMLTagNames.MAXERROR.equals(key)) {
                maxError = unpacker.unpackFloat();
            } else if (StationXMLTagNames.COEFFICIENT.equals(key)) {
                int size = unpacker.unpackArrayHeader();
                for (int j = 0; j < size; j++) {
                    coefficientList.add(unpackFloatNoUnitType(unpacker));
                }
            } else {
                throw new IOException("unknown key in object: " + key);
            }
        }
        return new Polynomial(resourceId, 
                                name, 
                                description, 
                                inputUnits, 
                                outputUnits, 
                                approximationType,
                                freqLowerBound,
                                freqUpperBound,
                                approxLowerBound,
                                approxUpperBound,
                                maxError, 
                                coefficientList);
        }

    protected static MessageBufferPacker packFIR(MessageBufferPacker packer, FIR fir) throws IOException {
        int objSize = 6;// required: all but description
        if (fir.getDescription() != null) {objSize++;}
        packer.packMapHeader(objSize);
        packer.packString(StationXMLTagNames.RESOURCEID).packString(fir.getResourceId());
        packer.packString(StationXMLTagNames.NAME).packString(fir.getName());
        if (fir.getDescription() != null) {
            packer.packString(StationXMLTagNames.DESCRIPTION).packString(fir.getDescription());
        }
        packer.packString(StationXMLTagNames.INPUTUNITS);
        packUnit(packer, fir.getInputUnits());
        packer.packString(StationXMLTagNames.OUTPUTUNITS);
        packUnit(packer, fir.getOutputUnits());
        packer.packString(StationXMLTagNames.SYMMETRY).packString(fir.getSymmetry());
        packer.packString(StationXMLTagNames.NUMERATORCOEFFICIENT).packArrayHeader(fir.getNumeratorCoefficientList().size());      
        for (Float f : fir.getNumeratorCoefficientList()) {
            packer.packFloat(f);
        }
        return packer;
    }

    protected static FIR unpackFIR(MessageUnpacker unpacker) throws IOException {
        String resourceId = null;
        String description = null;
        String name = null;
        Unit inputUnits = null;
        Unit outputUnits = null;
        String symmetry = null;
        List<Float> numeratorCoefficientList = new ArrayList<Float>();
        int objSize = unpacker.unpackMapHeader();
        for (int i = 0; i < objSize; i++) {
            String key = unpacker.unpackString();
            if (StationXMLTagNames.NUMERATORCOEFFICIENT.equals(key)) {
                // do first for speed as is repeated
                numeratorCoefficientList.add(unpacker.unpackFloat());
            } else if (StationXMLTagNames.RESOURCEID.equals(key)) {
                resourceId = unpacker.unpackString();
            } else if (StationXMLTagNames.NAME.equals(key)) {
                name = unpacker.unpackString();
            } else if (StationXMLTagNames.DESCRIPTION.equals(key)) {
                description = unpacker.unpackString();
            } else if (StationXMLTagNames.INPUTUNITS.equals(key)) {
                inputUnits = unpackUnit(unpacker);
            } else if (StationXMLTagNames.OUTPUTUNITS.equals(key)) {
                outputUnits = unpackUnit(unpacker);
            } else if (StationXMLTagNames.SYMMETRY.equals(key)) {
                symmetry = unpacker.unpackString();
            } else {
                throw new IOException("unknown key in object: " + key);
            }
        }
        return new FIR(resourceId, name, description, inputUnits, outputUnits, symmetry, numeratorCoefficientList);
    }

    protected static MessageBufferPacker packDecimation(MessageBufferPacker packer, Decimation decimation)
            throws IOException {
        int objSize = 5;// required: value, frequency
        packer.packMapHeader(objSize);
        packer.packString(StationXMLTagNames.INPUTSAMPLERATE).packFloat(decimation.getInputSampleRate());
        packer.packString(StationXMLTagNames.FACTOR).packFloat(decimation.getFactor());
        packer.packString(StationXMLTagNames.OFFSET).packFloat(decimation.getOffset());
        packer.packString(StationXMLTagNames.DELAY);
        packFloatType(packer, decimation.getDelay());
        packer.packString(StationXMLTagNames.CORRECTION);
        packFloatType(packer, decimation.getCorrection());
        return packer;
    }

    protected static Decimation unpackDecimation(MessageUnpacker unpacker) throws IOException {
        float inputSampleRate = Float.NaN;
        int factor = 0;
        int offset = 0;
        FloatType delay = null;
        FloatType correction = null;
        int objSize = unpacker.unpackMapHeader();
        for (int i = 0; i < objSize; i++) {
            String key = unpacker.unpackString();
            if (StationXMLTagNames.INPUTSAMPLERATE.equals(key)) {
                inputSampleRate = unpacker.unpackFloat();
            } else if (StationXMLTagNames.FACTOR.equals(key)) {
                factor = unpacker.unpackInt();
            } else if (StationXMLTagNames.OFFSET.equals(key)) {
                offset = unpacker.unpackInt();
            } else if (StationXMLTagNames.DELAY.equals(key)) {
                delay = unpackFloatType(unpacker);
            } else if (StationXMLTagNames.CORRECTION.equals(key)) {
                correction = unpackFloatType(unpacker);
            } else {
                throw new IOException("unknown key in object: " + key);
            }
        }
        return new Decimation(inputSampleRate, factor, offset, delay, correction);
    }

    protected static MessageBufferPacker packGainSensitivity(MessageBufferPacker packer, GainSensitivity gain)
            throws IOException {
        int objSize = 2;// required: value, frequency
        packer.packMapHeader(objSize);
        packer.packString(StationXMLTagNames.VALUE).packFloat(gain.getSensitivityValue());
        packer.packString(StationXMLTagNames.FREQUENCY).packFloat(gain.getFrequency());
        return packer;
    }

    protected static GainSensitivity unpackGainSensitivity(MessageUnpacker unpacker) throws IOException {
        float sensitivityValue = Float.NaN;
        float frequency = Float.NaN;
        int objSize = unpacker.unpackMapHeader();
        for (int i = 0; i < objSize; i++) {
            String key = unpacker.unpackString();
            if (StationXMLTagNames.VALUE.equals(key)) {
                sensitivityValue = unpacker.unpackFloat();
            } else if (StationXMLTagNames.FREQUENCY.equals(key)) {
                frequency = unpacker.unpackFloat();
            }
        }
        return new GainSensitivity(sensitivityValue, frequency);
    }

    protected static MessageBufferPacker packSensitivity(MessageBufferPacker packer, InstrumentSensitivity sensitivity)
            throws IOException {
        int objSize = 2;// required: value, frequency
        if (sensitivity.getFrequencyStart() != 0) {
            objSize++;
        }
        if (sensitivity.getFrequencyEnd() != 0) {
            objSize++;
        }
        if (sensitivity.getFrequencyDbVariation() != 0) {
            objSize++;
        }
        packer.packMapHeader(objSize);
        packer.packString(StationXMLTagNames.VALUE).packFloat(sensitivity.getSensitivityValue());
        packer.packString(StationXMLTagNames.FREQUENCY).packFloat(sensitivity.getFrequency());
        if (sensitivity.getFrequencyStart() != 0) {
            packer.packString(StationXMLTagNames.FREQUENCYSTART).packFloat(sensitivity.getFrequencyStart());
        }
        if (sensitivity.getFrequencyEnd() != 0) {
            packer.packString(StationXMLTagNames.FREQUENCYEND).packFloat(sensitivity.getFrequencyEnd());
        }
        if (sensitivity.getFrequencyDbVariation() != 0) {
            packer.packString(StationXMLTagNames.FREQUENCYDBVARIATION).packFloat(sensitivity.getFrequencyDbVariation());
        }
        return packer;
    }

    protected static InstrumentSensitivity unpackSensitivity(MessageUnpacker unpacker) throws IOException {
        Unit inputUnits = null;
        Unit outputUnits = null;
        float frequencyStart = Float.NaN;
        float frequencyEnd = Float.NaN;
        float frequencyDbVariation = Float.NaN;
        float sensitivityValue = Float.NaN;
        float frequency = Float.NaN;
        int objSize = unpacker.unpackMapHeader();
        for (int i = 0; i < objSize; i++) {
            String key = unpacker.unpackString();
            if (StationXMLTagNames.VALUE.equals(key)) {
                sensitivityValue = unpacker.unpackFloat();
            } else if (StationXMLTagNames.FREQUENCY.equals(key)) {
                frequency = unpacker.unpackFloat();
            } else if (StationXMLTagNames.INPUTUNITS.equals(key)) {
                inputUnits = unpackUnit(unpacker);
            } else if (StationXMLTagNames.OUTPUTUNITS.equals(key)) {
                outputUnits = unpackUnit(unpacker);
            } else if (StationXMLTagNames.FREQUENCYSTART.equals(key)) {
                frequencyStart = unpacker.unpackFloat();
            } else if (StationXMLTagNames.FREQUENCYEND.equals(key)) {
                frequencyEnd = unpacker.unpackFloat();
            } else if (StationXMLTagNames.FREQUENCYDBVARIATION.equals(key)) {
                frequencyDbVariation = unpacker.unpackFloat();
            }
        }
        return new InstrumentSensitivity(sensitivityValue,
                                         frequency,
                                         inputUnits,
                                         outputUnits,
                                         frequencyStart,
                                         frequencyEnd,
                                         frequencyDbVariation);
    }

    protected static MessageBufferPacker packUnit(MessageBufferPacker packer, Unit unit) throws IOException {
        int objSize = 1; // required: name
        if (unit.getDescription() != null) {
            objSize++;
        }
        packer.packMapHeader(objSize);
        packer.packString(StationXMLTagNames.NAME).packString(unit.getName());
        if (unit.getDescription() != null) {
            packer.packString(StationXMLTagNames.DESCRIPTION).packString(unit.getDescription());
        }
        return packer;
    }

    protected static Unit unpackUnit(MessageUnpacker unpacker) throws IOException {
        String name = null;
        String description = null;
        int objSize = unpacker.unpackMapHeader();
        for (int i = 0; i < objSize; i++) {
            String key = unpacker.unpackString();
            if (StationXMLTagNames.NAME.equals(key)) {
                name = unpacker.unpackString();
            } else if (StationXMLTagNames.DESCRIPTION.equals(key)) {
                description = unpacker.unpackString();
            }
        }
        return new Unit(name, description);
    }

    protected static MessageBufferPacker packFloatType(MessageBufferPacker packer, FloatType floatType)
            throws IOException {
        int objSize = 1; // required: value
        if (floatType.getUnit() != null) {
            objSize++;
        }
        if (floatType.getPlusError() != null) {
            objSize++;
        }
        if (floatType.getMinusError() != null) {
            objSize++;
        }
        packer.packMapHeader(objSize);
        packer.packString(StationXMLTagNames.VALUE).packFloat(floatType.getValue());
        if (floatType.getUnit() != null) {
            packer.packString(StationXMLTagNames.UNIT).packString(floatType.getUnit());
        }
        if (floatType.getPlusError() != null) {
            packer.packString(StationXMLTagNames.PLUSERROR).packFloat(floatType.getPlusError());
        }
        if (floatType.getMinusError() != null) {
            packer.packString(StationXMLTagNames.MINUSERROR).packFloat(floatType.getMinusError());
        }
        return packer;
    }

    protected static FloatNoUnitType unpackFloatNoUnitType(MessageUnpacker unpacker) throws IOException {
        float value = Float.NaN;
        Float plusError = null;
        Float minusError = null;
        int objSize = unpacker.unpackMapHeader();
        for (int i = 0; i < objSize; i++) {
            String key = unpacker.unpackString();
            if (StationXMLTagNames.VALUE.equals(key)) {
                value = unpacker.unpackFloat();
            } else if (StationXMLTagNames.PLUSERROR.equals(key)) {
                plusError = unpacker.unpackFloat();
            } else if (StationXMLTagNames.MINUSERROR.equals(key)) {
                minusError = unpacker.unpackFloat();
            } else {}
        }
        return new FloatNoUnitType(value, plusError, minusError);
    }

    protected static MessageBufferPacker packFloatNoUnitType(MessageBufferPacker packer, FloatNoUnitType floatType)
            throws IOException {
        int objSize = 1; // required: value
        if (floatType.getPlusError() != null) {
            objSize++;
        }
        if (floatType.getMinusError() != null) {
            objSize++;
        }
        packer.packMapHeader(objSize);
        packer.packString(StationXMLTagNames.VALUE).packFloat(floatType.getValue());
        if (floatType.getPlusError() != null) {
            packer.packString(StationXMLTagNames.PLUSERROR).packFloat(floatType.getPlusError());
        }
        if (floatType.getMinusError() != null) {
            packer.packString(StationXMLTagNames.MINUSERROR).packFloat(floatType.getMinusError());
        }
        return packer;
    }

    protected static FloatType unpackFloatType(MessageUnpacker unpacker) throws IOException {
        float value = Float.NaN;
        String unit = null;
        Float plusError = null;
        Float minusError = null;
        int objSize = unpacker.unpackMapHeader();
        for (int i = 0; i < objSize; i++) {
            String key = unpacker.unpackString();
            if (StationXMLTagNames.VALUE.equals(key)) {
                value = unpacker.unpackFloat();
            } else if (StationXMLTagNames.UNIT.equals(key)) {
                unit = unpacker.unpackString();
            } else if (StationXMLTagNames.PLUSERROR.equals(key)) {
                plusError = unpacker.unpackFloat();
            } else if (StationXMLTagNames.MINUSERROR.equals(key)) {
                minusError = unpacker.unpackFloat();
            } else {}
        }
        return new FloatType(value, unit, plusError, minusError);
    }

    /*
    protected static MessageBufferPacker packSOMETHING(MessageBufferPacker packer, SOMETHING polesZeros)
            throws IOException {
        return packer;
    }

    protected static SOMETHING unpackSOMETHING(MessageUnpacker unpacker) throws IOException {}
*/
    public static Response unpackResponse(MessageUnpacker unpacker) throws IOException {
        int objSize = unpacker.unpackMapHeader();
        List<ResponseStage> stageList = new ArrayList<ResponseStage>();
        InstrumentSensitivity sensitivity = null;
        InstrumentPolynomial polynomial = null; // not used yet
        for (int i = 0; i < objSize; i++) {
            String key = unpacker.unpackString();
            if (StationXMLTagNames.RESPONSESTAGE.equals(key)) {
                int numStages = unpacker.unpackArrayHeader();
                stageList = new ArrayList<ResponseStage>(numStages);
                for (int j = 0; j < numStages; j++) {
                    stageList.add(unpackStage(unpacker));
                }
            } else if (StationXMLTagNames.INSTRUMENT_SENSITIVITY.equals(key)) {
                sensitivity = unpackSensitivity(unpacker);
            }
        }
        return new Response(stageList, sensitivity, polynomial);
    }

    protected Response getResponseFromBlob(byte[] byteArray) throws IOException {
        if (byteArray.length > 0) {
            MessageUnpacker unpacker = MessagePack.newDefaultUnpacker(byteArray);
            return unpackResponse(unpacker);
        } else {
            return null;
        }
    }

    public Response getResponse() {
        return response;
    }

    public Channel getChannel() {
        return chan;
    }

    public void setChannel(Channel chan) {
        this.chan = chan;
    }

    Channel chan;

    Response response;

    int dbid;

    public int getDbid() {
        return dbid;
    }

    public void setDbid(int dbid) {
        this.dbid = dbid;
    }

    static ORB orb = null;
}
