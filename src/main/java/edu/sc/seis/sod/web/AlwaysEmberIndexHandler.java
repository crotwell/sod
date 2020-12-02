package edu.sc.seis.sod.web;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.URL;
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
            String requestUrlStr = request.getRequestURL().toString();

            URL parsedUrl = new URL(requestUrlStr);
            String path = parsedUrl.getPath();
            if (path.startsWith(WebAdmin.API)) {
            	logger.info("Api path, skipping...");
            	return;
            }
            if (path.equals("/favicon.ico")) {
            	response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            	baseRequest.setHandled(true);
            	return;
            } 

            logger.info("GET: (as alwaysEmberIndex)" + requestUrlStr);
            logger.info("path: "+path);
            Matcher matcher = indexRE.matcher(path);
            Matcher assetMatcher = assetsRE.matcher(path);
            if (path.length() == 0 || inTopLevelRoute(path) || matcher.matches()) {
            	logger.info("return index");
            	loadIndexHtml();
            	response.setContentType("text/html");
            	response.setStatus(HttpServletResponse.SC_OK);
            	PrintWriter out = response.getWriter();
            	out.print(indexHtml);
            	out.close();
            	baseRequest.setHandled(true);
            } else if (assetMatcher.matches()) {
            	String assetName = assetMatcher.group(1);
            	if ( ! assetExists(assetName)) {

                	logger.info("GET: (as alwaysEmberIndex), cannot find asset " + requestUrlStr);

                    super.handle(target, baseRequest, request, response);
                    return;
            	}
            	if (assetName.endsWith(".js")) {
                	response.setContentType("application/javascript");
            	} else if (assetName.endsWith(".css")) {
                	response.setContentType("text/css");
            	} else if (assetName.endsWith(".png")) {
                	response.setContentType("image/png");
            	} else if (assetName.endsWith(".html")) {
                	response.setContentType("text/html");
            	} else {
                	//response.setContentType("text/plain");
            	}
            	response.setStatus(HttpServletResponse.SC_OK);
            	OutputStream out = response.getOutputStream();
            	loadAsset(assetName, out);
            	baseRequest.setHandled(true);
            	out.close();
            } else {
            	logger.info("GET: (as alwaysEmberIndex), not index or asset " + requestUrlStr);

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
        	ClassLoader cl = this.getClass().getClassLoader();
        	URL htmlLocation = cl.getResource("META-INF/resources/webroot/index.html");
        	if (htmlLocation != null) {
        		logger.info("Found webroot in META-INF, loading index.html");
        		InputStream input = htmlLocation.openStream();
	            BufferedReader reader = new BufferedReader(new InputStreamReader(input));
	            char[] cbuf = new char[4096];
	            StringBuffer temp = new StringBuffer();
	            int nchars=0;
	            while ((nchars = reader.read(cbuf)) > 0) {
	                temp.append(cbuf, 0, nchars);
	            }
	            indexHtml = temp.toString();
	            reader.close();
        	} else {
        		indexHtml = "<html><body><h3>Error:</h3><p>No site/index.html found in current directory?</p></body></html>";
        	}
        }
    }
    
    boolean inTopLevelRoute(String path) {
    	if (path.equals("/")) {
    		return true;
    	}
    	for (String route : topLevelRoutes) {
			if (path.startsWith(route)) {
				return true;
			}
		}
    	return false;
    }

    boolean assetExists(String asset) {
    	ClassLoader cl = this.getClass().getClassLoader();
    	URL htmlLocation = cl.getResource("META-INF/resources/webroot/assets/"+asset);
    	return htmlLocation != null;
    }

    void loadAsset(String asset, OutputStream out) throws IOException {
    	String assetOut = "<html><body><h3>Error:</h3><p>No asset "+asset+" found in current directory?</p></body></html>";
    	ClassLoader cl = this.getClass().getClassLoader();
    	URL htmlLocation = cl.getResource("META-INF/resources/webroot/assets/"+asset);
    	if (htmlLocation != null) {
    		InputStream input = htmlLocation.openStream();
    		BufferedInputStream reader = new BufferedInputStream(input);
    		char[] cbuf = new char[4096];
    		StringBuffer temp = new StringBuffer();
    		int nb = 0;
    		long numTrans = reader.transferTo(out);
    		reader.close();
    	} else {
    		OutputStreamWriter write = new OutputStreamWriter(out);
    		write.write(assetOut);
    	}
    }

    String[] topLevelRoutes = { "/networks", "/quakes", "/stations", "/perusals", "/recipe", "/arms", "/quake-stations" };

    Pattern indexRE = Pattern.compile(".*/index.html");

    Pattern assetsRE = Pattern.compile(".*/assets/(.*)");

    String indexHtml = null;
    
    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(AlwaysEmberIndexHandler.class);
}
