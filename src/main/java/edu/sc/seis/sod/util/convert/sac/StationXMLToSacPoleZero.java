package edu.sc.seis.sod.util.convert.sac;

import edu.sc.seis.seisFile.fdsnws.stationxml.PolesZeros;
import edu.sc.seis.seisFile.fdsnws.stationxml.Response;
import edu.sc.seis.seisFile.fdsnws.stationxml.ResponseStage;
import edu.sc.seis.seisFile.fdsnws.stationxml.StationXMLException;
import edu.sc.seis.seisFile.fdsnws.stationxml.StationXMLTagNames;
import edu.sc.seis.seisFile.sac.Complex;
import edu.sc.seis.seisFile.sac.SacPoleZero;
import edu.sc.seis.sod.model.common.QuantityImpl;
import edu.sc.seis.sod.model.common.UnitImpl;
import edu.sc.seis.sod.model.common.UnknownUnit;
import edu.sc.seis.sod.util.convert.stationxml.StationXMLToFissures;

public class StationXMLToSacPoleZero {

    /** converts to Sac PoleZero from a StationXML Response. 
     * 
     * @throws UnknownUnit on bad unit conversion
     * */
    public static SacPoleZero convert(Response response) throws UnknownUnit {
        ResponseStage first = response.getFirstStage();
        if (first.getResponseItem() instanceof PolesZeros) {
            throw new IllegalArgumentException("First Stage is not PolesZeros: "+first.getResponseItem().getClass().getSimpleName());
        }
        PolesZeros polesZeros = (PolesZeros)first.getResponseItem();
        int gamma = 0;
        UnitImpl unit = StationXMLToFissures.convertUnit(polesZeros.getInputUnits());

        QuantityImpl scaleUnit = new QuantityImpl(1, unit);
        if (unit.isConvertableTo(UnitImpl.METER)) {
            gamma = 0;
            scaleUnit = scaleUnit.convertTo(UnitImpl.METER);
        } else if (unit.isConvertableTo(UnitImpl.METER_PER_SECOND)) {
            gamma = 1;
            scaleUnit = scaleUnit.convertTo(UnitImpl.METER_PER_SECOND);
        } else if (unit.isConvertableTo(UnitImpl.METER_PER_SECOND_PER_SECOND)) {
            gamma = 2;
            scaleUnit = scaleUnit.convertTo(UnitImpl.METER_PER_SECOND_PER_SECOND);
        } else {
            throw new IllegalArgumentException("response unit is not displacement, velocity or acceleration: "+unit);
        }
        int num_zeros = polesZeros.getZeroList().size() + gamma;
        double mulFactor = 1;
        if (polesZeros.getPzTransferType().equals(StationXMLTagNames.POLEZERO_LAPLACE_HERTZ)) {
            mulFactor = 2 * Math.PI;
        }
        Complex[] zeros = SacPoleZero.initCmplx(num_zeros);
        // extra gamma zeros are all (0,0)
        for (int i = 0; i < polesZeros.getZeroList().size(); i++) {
            zeros[i] = new Complex(polesZeros.getZeroList().get(i).getReal() * mulFactor,
                                   polesZeros.getZeroList().get(i).getImaginary() * mulFactor);
        }
        Complex[] poles = SacPoleZero.initCmplx(polesZeros.getPoleList().size());
        for (int i = 0; i < poles.length; i++) {
            poles[i] = new Complex(polesZeros.getPoleList().get(i).getReal() * mulFactor,
                                   polesZeros.getPoleList().get(i).getImaginary() * mulFactor);
        }
        float constant = polesZeros.getNormalizationFactor();
        double sd = response.getInstrumentSensitivity().getSensitivityValue();
        double fs = response.getInstrumentSensitivity().getFrequency();
        sd *= Math.pow(2 * Math.PI * fs, gamma);
        double A0 = polesZeros.getNormalizationFactor();
        double fn = polesZeros.getNormalizationFreq();
        A0 = A0 / Math.pow(2 * Math.PI * fn, gamma);
        if (polesZeros.getPzTransferType().equals(StationXMLTagNames.POLEZERO_LAPLACE_HERTZ)) {
            A0 *= Math.pow(2 * Math.PI, polesZeros.getPoleList().size() - polesZeros.getZeroList().size());
        }
        if (poles.length == 0 && zeros.length == 0) {
            constant = (float) (sd * A0);
        } else {
            constant = (float) (sd * calc_A0(poles, zeros, fs));
        }
        constant *= scaleUnit.getValue();
        return new SacPoleZero(poles, zeros, constant);
    }
    

    
    private static double calc_A0(Complex[] poles, Complex[] zeros, double ref_freq) {
        int i;
        Complex numer = ONE;
        Complex denom = ONE;
        Complex f0;
        double a0;
        f0 = new Complex(0, 2 * Math.PI * ref_freq);
        for (i = 0; i < zeros.length; i++) {
            denom = Complex.mul(denom, Complex.sub(f0, zeros[i]));
        }
        for (i = 0; i < poles.length; i++) {
            numer = Complex.mul(numer, Complex.sub(f0, poles[i]));
        }
        a0 = Complex.div(numer, denom).mag();
        return a0;
    }

    private static Complex ONE = new Complex(1,0);
}
