package edu.sc.seis.sod.status;
import edu.sc.seis.sod.CommonAccess;
import edu.sc.seis.sod.SodUtil;
import edu.sc.seis.sod.Start;
import edu.sc.seis.sod.status.eventArm.MapEventStatus;
import java.io.IOException;
import org.w3c.dom.Element;
import edu.sc.seis.fissuresUtil.exceptionHandler.GlobalExceptionHandler;
import edu.sc.seis.fissuresUtil.exceptionHandler.HTMLReporter;
import java.io.File;



public class IndexTemplate extends FileWritingTemplate{
    public IndexTemplate(String dirName) throws IOException{
        super(dirName + "/index.html");
        try {
            initExceptionHandler();
            Element template = TemplateFileLoader.getTemplate(getClass().getClassLoader(),
                                                              indexLoc);
            parse(template);
            write();
            SodUtil.copyFile(cssLoc, dirName+"/main.css");
            SodUtil.copyFile(Start.getConfigFileName(), dirName + "/" + Start.getConfigFileName());
        } catch (Exception e) {
            CommonAccess.handleException("unexpected problem creating index.html page", e);
        }
    }

    protected Object getTemplate(String tagName, Element el){
        if(tagName.equals("eventMap")){
            MapEventStatus mes = new MapEventStatus(el, true);
            return new RelativeLocationTemplate(getOutputLocation(),
                                                mes.getLocation());
        }
        return super.getTemplate(tagName, el);
    }

    protected void initExceptionHandler() throws IOException {
        File errorDir = new File(getOutputDirectory(), "Errors");
        errorDir.mkdirs();
        GlobalExceptionHandler.add(new HTMLReporter(errorDir));
    }

    private static String indexLoc = "jar:edu/sc/seis/sod/data/templates/index.xml";

    private static String cssLoc = "jar:edu/sc/seis/sod/data/templates/main.css";
}

