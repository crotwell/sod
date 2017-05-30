package edu.sc.seis.sod.hibernate;

import java.util.Timer;
import java.util.TimerTask;

class PrintIfNotCalledOff extends TimerTask {

    PrintIfNotCalledOff(String msg) {
        this(msg, 5);
    }

    PrintIfNotCalledOff(String msg, int delaySeconds) {
        this.msg = msg;
        timer.schedule(this, delaySeconds * 1000);
    }

    public void callOff() {
        calledOff = true;
    }

    public void run() {
        if (!calledOff) {
            logger.info(msg);
        }
    }
    
    static Timer timer = new Timer("PrintIfNotCalledOff", true);

    String msg;

    boolean calledOff = false;

    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(PrintIfNotCalledOff.class);
}