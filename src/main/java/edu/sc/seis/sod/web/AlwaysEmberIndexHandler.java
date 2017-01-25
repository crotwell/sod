package edu.sc.seis.sod.web;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;

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
            loadIndexHtml();
            PrintWriter out = response.getWriter();
            out.print(indexHtml);
            response.setStatus(HttpServletResponse.SC_OK);
            out.close();
            baseRequest.setHandled(true);
        } else {
            super.handle(target, baseRequest, request, response);
            return;
        }
    }

    void loadIndexHtml() throws IOException {
        if (indexHtml == null) {
            BufferedReader reader = new BufferedReader(new FileReader(WebAdmin.SITE + "/index.html"));
            char[] cbuf = new char[4096];
            String temp = "";
            while (reader.read(cbuf) > 0) {
                temp += cbuf;
            }
            indexHtml = temp;
        }
    }

    String indexHtml = null;
    
    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(AlwaysEmberIndexHandler.class);
}
