#! /usr/bin/python -O

import sodBuilder
import sys, os, time, zipfile, tarfile
sys.path.append("../devTools/maven")
sys.path.append("./scripts")
import distBuilder, sodScriptBuilder, ProjectParser

def buildInternal(proj, name=None):
    scripts=sodScriptBuilder.buildAll(proj)
    name =  'internal' + buildName(proj)
    extras = [('scripts/revtest.xml', 'bin/revtest.xml'),
              ('scripts/tutorial.xml', 'bin/tutorial.xml'),
              ('scripts/weed.xml', 'bin/weed.xml'),
              ('scripts/yjpagent.dll', 'bin/yjpagent.dll'),
              ('scripts/cwg.prop', 'bin/cwg.prop'),
              ('scripts/logs', 'bin/logs', False)]
    buildDist(proj, scripts, name, extras)

def buildExternal(proj, name=None):
    scripts=sodScriptBuilder.buildSodScripts(proj)
    scripts.extend(sodScriptBuilder.buildEditorScripts(proj))
    extras = [('scripts/tutorial.xml', 'docs/tutorial.xml'),
              ('scripts/weed.xml', 'docs/weed.xml'),
              ('site/generatedSite', 'docs')]
    name = buildName(proj)
    zip = zipfile.ZipFile(name + ".zip", 'w')
    tar = tarfile.open(name + '.tar', 'w')
    buildDist(proj, scripts, name, extras, [tar, zip])

def buildDist(proj, scripts, name=None, extras=[], archives=[]):
    sodBuilder.build(proj)
    if not os.path.exists('scripts/logs'): os.mkdir('scripts/logs')
    scriptsWithTarloc = [(script, 'bin/'+script) for script in scripts]
    if name is None: name = buildName(proj)
    distBuilder.buildDist(proj, archives, True, extras, scriptsWithTarloc, name)

def buildName(proj):
    return proj.name + '-' + time.strftime('%y%m%d')

if __name__ == "__main__":
    sodScriptBuilder.sodScriptParameters.homeloc='../'
    proj = ProjectParser.ProjectParser('./project.xml')
    if len(sys.argv) > 1:  buildExternal(proj)
    else: buildInternal(proj)
