import sys, os
sys.path.append("../devTools/maven")
sys.path.append("./scripts")
import distBuilder, sodScriptBuilder, ProjectParser

def buildDist(proj):
    extras = [('scripts/yjpagent.dll', 'yjpagent.dll'),
              ('scripts/revtest.xml', 'revtest.xml'),
              ('scripts/cwg.prop', 'cwg.prop'),
              ('scripts/logs', 'logs', False)]
    scripts = sodScriptBuilder.buildSodScripts(proj)
    distBuilder.buildDist(proj, True, extras, scripts)

if __name__ == "__main__":
    buildDist(ProjectParser.ProjectParser('./project.xml'))