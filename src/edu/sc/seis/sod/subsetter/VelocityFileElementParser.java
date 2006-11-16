package edu.sc.seis.sod.subsetter;

import java.io.File;
import org.w3c.dom.Element;
import edu.sc.seis.fissuresUtil.display.configuration.DOMHelper;

public class VelocityFileElementParser {

    /**
     * Uses the contents of the workingDir element in config as workingDir, or
     * if it doesn't exist, uses defaultWorkingDir Uses the contents of the
     * location element in config as location, or if it doesn't exist, uses
     * defaultLocation
     */
    public VelocityFileElementParser(Element config,
                                     String defaultWorkingDir,
                                     String defaultLocation) {
        this(DOMHelper.extractText(config, "workingDir", defaultWorkingDir),
             DOMHelper.extractText(config, "location", defaultLocation));
    }

    /**
     * Takes a workingDir and location and ensures that workingDir ends with or
     * location starts with File.separator
     */
    public VelocityFileElementParser(String workingDir, String location) {
        this.workingDir = workingDir.trim();
        this.location = location.trim();
        // Explicitly check for / slash since \ is separator under windows, but
        // / can be used just as well
        if(workingDir.endsWith(File.separator) || workingDir.endsWith("/")) {
            if(location.startsWith(File.separator) || location.startsWith("/")) {
                this.location = location.substring(1);
            }
        } else if(workingDir.length() > 0
                && !(location.startsWith(File.separator) || location.startsWith("/"))) {
            this.workingDir += File.separator;
        }
    }

    public String getTemplate() {
        return getWorkingDir() + getLocation();
    }

    public String getWorkingDir() {
        return workingDir;
    }

    public String getLocation() {
        return location;
    }

    private String location, workingDir;
}
