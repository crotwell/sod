package edu.sc.seis.sod.status;

import edu.sc.seis.sod.SodUtil;

public class RelativeLocationTemplate extends AllTypeTemplate{
    public RelativeLocationTemplate(String src, String dest){
        location = SodUtil.getRelativePath(src, dest, "/");
    }
    
    public String getResult() { return location; }
    
    private String location;
}
