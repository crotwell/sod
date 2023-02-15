/**
 * ServletReporter.java
 *
 * @author Created by Omnicore CodeGuide
 */

package edu.sc.seis.sod.util.exceptionHandler;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



public class ServletReporter implements ExceptionReporter{
    
    public void report(String message, Throwable e, List sections) {
        try {
            URL url = new URL(System.getProperty("errorHandlerServlet"));
            HttpURLConnection http = (HttpURLConnection)url.openConnection();
            http.setRequestMethod("POST");
            http.setDoOutput(true);
            BufferedWriter out = new BufferedWriter( new OutputStreamWriter(http.getOutputStream()));
            out.write("bugreport="+ message);
            out.write(ExceptionReporterUtils.getTrace(e));
            out.write(ExceptionReporterUtils.getSysInfo());
            out.write("\r\n");
            out.close();
            http.connect();
            BufferedReader read = new BufferedReader(new InputStreamReader(http.getInputStream()));
            String s;
            while ((s = read.readLine()) != null) {
                logger.debug(s);
            }
            read.close();
        } catch (IOException ex) {
            logger.error("Problem sending error to server", ex);
        }
    }
    
    private static Logger logger = LoggerFactory.getLogger(ServletReporter.class);
}
