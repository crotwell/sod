package edu.sc.seis.sod.subsetter.station;

import org.w3c.dom.Element;

import edu.sc.seis.seisFile.fdsnws.stationxml.Equipment;
import edu.sc.seis.sod.model.station.StationImpl;
import edu.sc.seis.sod.source.network.NetworkSource;
import edu.sc.seis.sod.status.Fail;
import edu.sc.seis.sod.status.Pass;
import edu.sc.seis.sod.status.StringTree;
import edu.sc.seis.sod.subsetter.AbstractEquipmentSubsetter;

public class StationEquipment extends AbstractEquipmentSubsetter implements StationSubsetter {

    public StationEquipment() {
    }
    public StationEquipment(Element config) {
        super(config);
    }

    @Override
    public StringTree accept(StationImpl station, NetworkSource network) throws Exception {
        for (Equipment eq : station.getEquipment()) {
            if (eq != null && super.doesMatch(eq)) {
                return new Pass(this);
            }  
        }
        return new Fail(this);
    }
}
