#!/usr/bin/python -O
import sys, os, time, zipfile, tarfile
sys.path.append("../devTools/maven")
import ProjectParser, mavenExecutor, scriptBuilder, depCopy, distBuilder
from optparse import OptionParser

class sodScriptParameters(scriptBuilder.jacorbParameters):
    homeloc = '.'
    def __init__(self, mods):
        scriptBuilder.jacorbParameters.__init__(self)
        for mod in mods: self.update(mod)
        self.name = 'sod'
        homevar = self.add('SOD_HOME', self.homeloc, 'initial', 1, True)
        libvar = self.getVar('LIB', 'initial')
        libvar.setValue(homevar.interp+'/lib')
        self.mainclass = 'edu.sc.seis.sod.Start'
        self.xOptions['mx']='mx256m'

class queryTimer(sodScriptParameters):
    def __init__(self, mods):
        sodScriptParameters.__init__(self, [])
        for mod in mods: self.update(mod)
        self.mainclass='edu.sc.seis.sod.QueryTimer'
        self.name='queryTimer'

class editor(sodScriptParameters):
    def __init__(self, mods):
        sodScriptParameters.__init__(self, [])
        for mod in mods: self.update(mod)
        self.mainclass='edu.sc.seis.sod.editor.SodGUIEditor'
        self.name='sodeditor'

class kill(sodScriptParameters):
    def __init__(self, mods):
        sodScriptParameters.__init__(self, [])
        for mod in mods: self.update(mod)
        self.mainclass='edu.sc.seis.sod.SodKiller'
        self.name='killSod'


def buildAllScripts(proj):
    scripts = buildRunScripts(proj)
    scripts.extend(buildQueryTimerScripts(proj))
    scripts.extend(buildEditorScripts(proj))
    scripts.extend(buildProfileScripts(proj))
    scripts.extend(buildKillScripts(proj))
    return scripts

def buildRunScripts(proj):
    scriptBuilder.setVarSh()
    scripts = [scriptBuilder.build(sodScriptParameters([]), proj)]
    scriptBuilder.setVarWindows()
    scripts.append(scriptBuilder.build(sodScriptParameters([scriptBuilder.windowsParameters()]), proj))
    return scripts

def buildProfileScripts(proj):
    scriptBuilder.setVarWindows()
    profileParams = sodScriptParameters([scriptBuilder.profileParameters(), scriptBuilder.windowsParameters()])
    profileParams.name='profile'
    return [scriptBuilder.build(profileParams, proj)]

def buildQueryTimerScripts(proj):
    scriptBuilder.setVarSh()
    scripts = [scriptBuilder.build(queryTimer([]), proj)]
    scriptBuilder.setVarWindows()
    scripts.append(scriptBuilder.build(queryTimer([scriptBuilder.windowsParameters()]), proj))
    return scripts

def buildEditorScripts(proj):
    scriptBuilder.setVarSh()
    scripts = [scriptBuilder.build(editor([]), proj)]
    scriptBuilder.setVarWindows()
    scripts.append(scriptBuilder.build(editor([scriptBuilder.windowsParameters()]), proj))
    return scripts

def buildKillScripts(proj):
    scriptBuilder.setVarSh()
    scripts = [scriptBuilder.build(kill([]), proj)]
    scriptBuilder.setVarWindows()
    scripts.append(scriptBuilder.build(kill([scriptBuilder.windowsParameters()]), proj))
    return scripts

def buildJars(sodProj, clean=False):
    curdir = os.path.abspath('.')
    os.chdir(sodProj.path)
    allProj = [ProjectParser.ProjectParser('../fissures/project.xml'),
               ProjectParser.ProjectParser('../fissuresUtil/project.xml'),
               sodProj]
    if clean:
        for proj in allProj: mavenExecutor.mavenExecutor(proj).clean()
    for proj in allProj: mavenExecutor.mavenExecutor(proj).jarinst()
    os.chdir(curdir)

def buildInternalDist(proj, name):
    buildJars(proj)
    if name == buildName(proj): name = "internal" + name
    scripts = buildAll(proj)
    configs = []
    for item in os.listdir('scripts'):
        if item.endswith('.xml'): configs.append(item)
    extras = [('scripts/' + item, 'examples/' + item) for item in configs]
    extras.extend([('scripts/yjpagent.dll', 'yjpagent.dll'),
              ('scripts/cwg.prop', 'cwg.prop'),
              ('scripts/logs', 'logs', False)])
    buildDist(proj, scripts, name, extras)

def buildExternalDist(proj, name):
    buildJars(proj, True)
    scripts = buildRunScripts(proj)
    scripts.extend(buildEditorScripts(proj))
    os.chdir('site')
    print 'building docs'
    os.spawnlp(os.P_WAIT, 'buildSite.sh', 'sh', 'buildSite.sh')
    os.chdir('..')
    extras = [('scripts/tutorial.xml', 'examples/tutorial.xml'),
              ('scripts/weed.xml', 'examples/weed.xml'),
              ('scripts/legacyExecute.xml', 'examples/legacyExecute.xml'),
              ('scripts/legacyExecuteMoVec.xml', 'examples/legacyExecuteMoVec.xml'),
              ('scripts/realtime.xml', 'examples/realtime.xml'),
              ('scripts/breqfast.xml', 'examples/breqfast.xml'),
              ('site/generatedSite', 'docs')]
    zip = zipfile.ZipFile(name + ".zip", 'w')
    tar = tarfile.open(name + '.tar', 'w')
    buildDist(proj, scripts, name, extras, [tar, zip])

def buildDist(proj, scripts, name=None, extras=[], archives=[]):
    if not os.path.exists('scripts/logs'): os.mkdir('scripts/logs')
    extras.extend([(script, 'bin/'+script) for script in scripts])
    if name is None: name = buildName(proj)
    distBuilder.buildDist(proj, extras, name, True, archives)
    for script in scripts: os.remove(script)

def buildName(proj): return proj.name + '-' + time.strftime('%y%m%d')

if __name__ == "__main__":
    proj = ProjectParser.ProjectParser('./project.xml')
    parser = OptionParser()
    parser.add_option("-d", "--dist", dest="external",
                      help="build dist for release containing example configs and docs",
                      default=False,
                      action="store_true")
    parser.add_option("-i", "--internal-dist", dest="internal",
                      help="build dist for internal usage containing all scripts, all configs, and yourkit profiling stuff",
                      default=False,
                      action="store_true")
    parser.add_option("-n", "--name", dest="name",
                      help="archive file name for use with -d and -i", metavar="NAME",
                      default=buildName(proj))
    parser.add_option("-s", "--scripts", dest="scripts",
                      help="compile sod and build run scripts(default option)",
                      default=True,
                      action="store_true")
    options = parser.parse_args()[0]
    if options.external : buildExternal(proj, options.name)
    elif options.internal: buildInternal(proj, options.name)
    else :
        buildJars(proj)
        os.chdir('scripts')
        buildAllScripts(proj)
        depCopy.copy(proj)

