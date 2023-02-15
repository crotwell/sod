package edu.sc.seis.sod.model.status;

import java.io.Serializable;

public class Status implements Serializable {

    protected Status() {
        // for hibernate
    }
    
    private Status(Stage stage, Standing standing){
        this.standing = standing;
        this.stage = stage;
    }

    public String toString(){
        return stage.toString()+" "+standing.toString();
    }

    public Standing getStanding(){ return standing; }

    public Stage getStage(){ return stage; }

    private Stage stage;
    private Standing standing;

    public short getAsShort() {
        return (short)((getStage().getVal()<<8)+getStanding().getVal());
    }

    public static Status getFromShort(short val) {
        return Status.get(Stage.getFromInt((val&0xFF00)>>>8),
                          Standing.getFromInt(val&0xFF));
    }

    public static Status get(Stage stage, Standing standing){
        return ALL[stage.getVal()][standing.getVal()];
    }
    
    // hibernate

    public int getStandingInt(){ return standing.getVal(); }
    
    protected void setStandingInt(int val) {
        standing = Standing.getFromInt(val);
    }

    public int getStageInt(){ return stage.getVal(); }
    
    protected void setStageInt(int val) {
        stage = Stage.getFromInt(val);
    }

    public static final Status[][] ALL = new Status[Stage.ALL.length][Standing.ALL.length];

    static {
        for (int stage = 0; stage < Stage.ALL.length; stage++) {
            for (int standing = 0; standing < Standing.ALL.length; standing++) {
                ALL[stage][standing] = new Status(Stage.getFromInt(stage), Standing.getFromInt(standing));
            }
        }
    }

    public static Status get(String nestedText) {
        for (int i = 0; i < ALL.length; i++) {
            for (int j = 0; j < ALL[i].length; j++) {
                if(ALL[i][j].toString().equals(nestedText)) return ALL[i][j];
            }
        }
        throw new IllegalArgumentException("No such status for string " + nestedText);
    }

}

