package edu.sc.seis.sod.web;

import java.io.File;

public class MeasurementToolServlet extends JsonToFileServlet {

    public MeasurementToolServlet() {
        super(WebAdmin.getApiBaseUrl(), new File("jsonData"), "measurement-tools");
        // TODO Auto-generated constructor stub
    }
}
