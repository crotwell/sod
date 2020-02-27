package edu.sc.seis.seiswww;

public class ExternalInfo {

    public ExternalInfo(String packageInSOD,
                        String interfaceName,
                        String ingredientLoc,
                        String usage) {
        this.interfaceURL = "http://www.seis.sc.edu/viewvc/seis/trunk/sod/src/main/java/edu/sc/seis/sod/"
                + packageInSOD + "/" + interfaceName + ".java?view=markup";
        this.name = "external" + interfaceName;
        this.interfaceName = interfaceName;
        this.ingredientURL = ingredientLoc + ".html";
        this.usage = usage;
        this.exampleURL = "http://www.seis.sc.edu/viewvc/seis/trunk/sod/externalExample/src/edu/sc/seis/sod/example/" + interfaceName + "Example.java?view=markup";
    }

    public String getIngredientURL() {
        return ingredientURL;
    }

    public String getExampleURL() {
        return exampleURL;
    }

    public String getInterfaceName() {
        return interfaceName;
    }

    public String getInterfaceURL() {
        return interfaceURL;
    }

    public String getName() {
        return name;
    }

    public String getUsage() {
        return usage;
    }

    public String getRelaxHTML(String pathToRoot) {
        return p(usage)
                + p("The class in classname must implement "
                        + a(interfaceURL, interfaceName) + " like this "
                        + a(exampleURL, "example implementation") + ".")
                + p("For more information on externals, see the "
                        + a(pathToRoot + "documentation/externals/index.html",
                            "externals documentation"));
    }

    protected String p(String content) {
        return "<p>" + content + "</p>\n";
    }

    protected String a(String url, String content) {
        return "<a href=\"" + url + "\">" + content + "</a>";
    }

    protected String interfaceURL, name, interfaceName, ingredientURL,
            usage, exampleURL;
}