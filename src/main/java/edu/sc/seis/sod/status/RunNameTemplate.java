package edu.sc.seis.sod.status;


import edu.sc.seis.sod.Start;

public class RunNameTemplate extends AllTypeTemplate{
    public String getResult() { return Start.getRunProps().getRunName();}
}

