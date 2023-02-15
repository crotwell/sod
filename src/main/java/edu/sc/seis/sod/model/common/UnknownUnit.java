package edu.sc.seis.sod.model.common;

import edu.sc.seis.seisFile.fdsnws.stationxml.Unit;

public class UnknownUnit extends Exception {

    public UnknownUnit(Unit unit) {
        this("Unit ("+unit.getName()+", "+unit.getDescription()+") is unknown.");
        this.unit = unit;
    }

    public UnknownUnit(String message) {
        super(message);
    }

    Unit unit;
}
