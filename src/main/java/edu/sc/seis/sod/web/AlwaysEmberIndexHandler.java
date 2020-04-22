package edu.sc.seis.sod.web;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.DefaultHandler;

public class AlwaysEmberIndexHandler extends DefaultHandler {

    @Override
    public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        if (request.getContextPath() == null || !request.getContextPath().startsWith(WebAdmin.API)) {
            String URL = request.getRequestURL().toString();
            logger.info("GET: (as alwaysEmberIndex)" + URL);
            Matcher matcher = indexRE.matcher(URL);
            if (matcher.matches()) {
            	loadIndexHtml();
            	response.setContentType("text/html");
            	response.setStatus(HttpServletResponse.SC_OK);
            	PrintWriter out = response.getWriter();
            	out.print(indexHtml);
            	out.close();
            	baseRequest.setHandled(true);
            } else {
            	logger.info("GET: (as alwaysEmberIndex), not index or asset" + URL);

                super.handle(target, baseRequest, request, response);
                return;
            }
        } else {
            super.handle(target, baseRequest, request, response);
            return;
        }
    }

    void loadIndexHtml() throws IOException {
        if (indexHtml == null) {
        	File indexFile = new File(WebAdmin.SITE + "/index.html");
        	if (indexFile.exists()) {
	            BufferedReader reader = new BufferedReader(new FileReader(WebAdmin.SITE + "/index.html"));
	            char[] cbuf = new char[4096];
	            StringBuffer temp = new StringBuffer();
	            while (reader.read(cbuf) > 0) {
	                temp.append(cbuf);
	            }
	            indexHtml = temp.toString();
        	} else {
        		indexHtml = "<html><body><h3>Error:</h3><p>No site/index.html found in current directory?</p></body></html>";
        	}
        }
    }


    Pattern indexRE = Pattern.compile("/index.html");

    Pattern assetsRE = Pattern.compile(".*/assets/(.*)");

    String indexHtml = null;
    
    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(AlwaysEmberIndexHandler.class);
}
