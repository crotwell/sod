#!/usr/bin/python -O
import sys, os, time, zipfile, tarfile
from optparse import OptionParser
startdir = os.getcwd()
buildPyDir = os.path.realpath(os.path.dirname(sys.argv[0]))
os.chdir(buildPyDir)
sys.path.append("../devTools/maven")
import ProjectParser, mavenExecutor, scriptBuilder, depCopy, distBuilder
os.chdir(startdir)

class sodScriptParameters(scriptBuilder.jacorbParameters):
    homeloc = '.'
    def __init__(self, mods, proj, mainclass='edu.sc.seis.sod.Start', name='sod', mavenRepoStructure=False):
        scriptBuilder.jacorbParameters.__init__(self)
        for mod in mods: self.update(mod)
        self.name = name
        homevar = self.add('SOD_HOME', self.homeloc, 'initial', 1, True)
        libvar = self.getVar('LIB', 'initial')
        if mavenRepoStructure:
            if os.environ.has_key('OS') and os.environ['OS'] == 'Windows_NT':
                stdin, stdout = os.popen2('cygpath -w ' + proj.repo)
                libvar.setValue(stdout.read())
            else:
                libvar.setValue(proj.repo)    
        else:
            libvar.setValue(homevar.interp+'/lib')    
        self.mainclass = mainclass
        self.xOptions['mx']='mx256m'

genericScripts = {'sod':'edu.sc.seis.sod.Start',
                  'queryTimer':'edu.sc.seis.sod.QueryTimer',
                  'sodeditor':'edu.sc.seis.sod.editor.SodGUIEditor',
                  'killSod':'edu.sc.seis.sod.SodKiller'}

def buildAllScripts(proj, useMavenJars=False):
    scripts = buildManyScripts(proj, genericScripts.keys(), useMavenJars)
    scripts.extend(buildProfileScripts(proj, useMavenJars))
    return scripts

def buildProfileScripts(proj, useMavenJars=False):
    scriptBuilder.setVarSh()
    profileParams = sodScriptParameters([scriptBuilder.sharkParameters()], proj, mavenRepoStructure=useMavenJars)
    profileParams.name='profile'
    scripts = [scriptBuilder.build(profileParams, proj)]
    scriptBuilder.setVarWindows()
    profileParams = sodScriptParameters([scriptBuilder.profileParameters(), scriptBuilder.windowsParameters()], proj, mavenRepoStructure=useMavenJars)
    profileParams.name='profile'
    scripts.append(scriptBuilder.build(profileParams, proj))
    return scripts

def buildManyScripts(proj, names, useMavenJars=False):
    scripts = []
    for name in names: scripts.extend(buildScripts(proj, name, mavenRepoStructure=useMavenJars))
    return scripts

def buildScripts(proj, name, main='', mavenRepoStructure=False):
    if main == '':main = genericScripts[name]
    scriptBuilder.setVarSh()
    params = sodScriptParameters([], proj, main, name, mavenRepoStructure)
    scripts = [scriptBuilder.build(params , proj, useMavenJars=mavenRepoStructure)]
    scriptBuilder.setVarWindows()
    params = sodScriptParameters([scriptBuilder.windowsParameters()], proj, main, name, mavenRepoStructure)
    scripts.append(scriptBuilder.build(params, proj, useMavenJars=mavenRepoStructure))
    return scripts

def buildJars(sodProj, clean=False):
    curdir = os.path.abspath('.')
    os.chdir(sodProj.path)
    allProj = [ProjectParser.ProjectParser('../fissures/project.xml'),
               ProjectParser.ProjectParser('../fissuresUtil/project.xml'),
               sodProj]
    compiled = False
    for proj in allProj:
        if clean:    
             mavenExecutor.mavenExecutor(proj).clean()
        if mavenExecutor.mavenExecutor(proj).jarinst():
            compiled = True
    os.chdir(curdir)
    return compiled

def buildInternalDist(proj, name):
    buildJars(proj)
    if name == buildName(proj): name = "internal" + name
    scripts = buildAllScripts(proj)
    configs = []
    scriptDir = buildPyDir + os.sep + 'scripts' + os.sep
    for item in os.listdir(scriptDir):
        if item.endswith('.xml'): configs.append(item)
    extras = [(scriptDir + item, 'examples/' + item) for item in configs]
    extras.extend([(scriptDir + 'yjpagent.dll', 'yjpagent.dll'),
              (scriptDir + 'cwg.prop', 'cwg.prop'),
              (scriptDir + 'logs', 'logs', False)])
    buildDist(proj, scripts, name, extras)

def buildExternalDist(proj, name, filthy):
    buildJars(proj, not filthy)
    scripts = buildManyScripts(proj, ['sod', 'sodeditor'])
    os.chdir(buildPyDir + os.sep + 'site')
    print 'building docs'
    os.spawnlp(os.P_WAIT, 'buildSite.sh', 'sh', 'buildSite.sh')
    os.chdir(startdir)
    extras = [('scripts/tutorial.xml', 'examples/tutorial.xml'),
              ('scripts/weed.xml', 'examples/weed.xml'),
              ('scripts/legacyExecute.xml', 'examples/legacyExecute.xml'),
              ('scripts/legacyVectorExecute.xml', 'examples/legacyVectorExecute.xml'),
              ('scripts/vector.xml', 'examples/vector.xml'),
              ('scripts/realtime.xml', 'examples/realtime.xml'),
              ('scripts/breqfast.xml', 'examples/breqfast.xml'),
              ('site/generatedSite', 'docs')]
    extras = [ (buildPyDir + os.sep + local, dist) for local, dist in extras ]    
    zip = zipfile.ZipFile(name + ".zip", 'w')
    tar = tarfile.open(name + '.tar', 'w')
    buildDist(proj, scripts, name, extras, [tar, zip])

def buildDist(proj, scripts, name=None, extras=[], archives=[]):
    if not os.path.exists(buildPyDir + os.sep + 'scripts/logs'): os.mkdir(buildPyDir + os.sep + 'scripts/logs')
    extras.extend([(script, 'bin/'+script) for script in scripts])
    if name is None: name = buildName(proj)
    distBuilder.buildDist(proj, extras, name, True, archives)
    for script in scripts: os.remove(script)

def buildName(proj): return proj.name + '-' + time.strftime('%y%m%d')

if __name__ == "__main__":
    proj = ProjectParser.ProjectParser(buildPyDir + os.sep + 'project.xml')
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
    parser.add_option("-c", "--copyjars", dest="copy",
                      help="copy the jars locally for use in the scripts",
                      default=False,
                      action="store_true")
    parser.add_option("-f", "--filthy", dest="filthy",
                      help="don't clean the jars before building",
                      default=False,
                      action="store_true")
    options = parser.parse_args()[0]
    if options.external : buildExternalDist(proj, options.name, options.filthy)
    elif options.internal: buildInternalDist(proj, options.name)
    else :
        buildJars(proj)
        buildAllScripts(proj, not options.copy)
        if options.copy:
            depCopy.copy(proj)
