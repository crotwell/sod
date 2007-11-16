package edu.sc.seis.sod.status.waveformArm;

import java.sql.SQLException;

import edu.sc.seis.sod.hibernate.SodDB;
import edu.sc.seis.sod.status.AllTypeTemplate;

public class NumSuccessfulECPTemplate extends AllTypeTemplate{
    public NumSuccessfulECPTemplate() throws SQLException{
        ecs = new SodDB();
    }

    public String getResult(){
        return "" + ecs.getNumSuccessful();
    }

    private SodDB ecs;
}

