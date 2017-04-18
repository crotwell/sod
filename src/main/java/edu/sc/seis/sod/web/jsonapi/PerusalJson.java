package edu.sc.seis.sod.web.jsonapi;


public class PerusalJson extends AbstractJsonApiData {

    public PerusalJson(String id, String baseUrl) {
        super(baseUrl);
        this.id = id;
    }

    @Override
    public String getType() {
        return "perusal";
    }

    @Override
    public String getId() {
        return id;
    }
    
    String id;
    
    int currentESPair;
    
    String order;
    
}
