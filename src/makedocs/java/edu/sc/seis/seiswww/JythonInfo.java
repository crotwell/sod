package edu.sc.seis.seiswww;


public class JythonInfo extends ExternalInfo {

    public JythonInfo(String packageInSOD,
                      String interfaceName,
                      String ingredientLoc,
                      String usage) {
        super(packageInSOD, interfaceName, ingredientLoc, usage);
        this.exampleURL = "http://www.seis.sc.edu/viewvc/seis/trunk/sod/jythonExample/" + interfaceName + "Example.py";
        
    }


    public String getRelaxHTML(String pathToRoot) {
        return p(usage)
                + p("The jython class defined in class must implement "
                        + a(interfaceURL, interfaceName) + " like this "
                        + a(exampleURL, "example implementation") + ".")
                + p("For more information on externals, see the "
                        + a(pathToRoot + "documentation/externals/index.html",
                            "externals documentation"));
    }
}
