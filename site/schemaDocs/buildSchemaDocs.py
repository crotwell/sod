#! /usr/bin/python -O
import sys, signal, os
sys.path.append('../../../devTools/maven')
import scriptBuilder, ProjectParser, depCopy
sys.path.append('../../')
import buildSod
class schemaDocsParams(scriptBuilder.jacorbParameters):
    homeloc = '.'
    def __init__(self, mods):
        scriptBuilder.jacorbParameters.__init__(self)
        for mod in mods: self.update(mod)
        self.name = 'schemaDocumenter'
        homevar = self.add('SOD_HOME', self.homeloc, 'initial', 1)
        libvar = self.getVar('LIB', 'initial')
        libvar.setValue(homevar.interp+'/lib')
        self.mainclass = 'edu.sc.seis.sod.validator.documenter.SchemaDocumenter'
        self.xOptions['mx']='mx512m'

def buildScripts(proj):
    scriptBuilder.setVarSh()
    scripts = [scriptBuilder.build(schemaDocsParams([]), proj)]
    scriptBuilder.setVarWindows()
    scripts.append(scriptBuilder.build(schemaDocsParams([scriptBuilder.windowsParameters()]), proj))
    return scripts

def signal_handler(signal, frame):
    sys.exit(0)

def main(argv):
    signal.signal(signal.SIGINT, signal_handler)
    startdir = os.path.abspath('.')
    command = 'schemaDocumenter.bat'
    proj = ProjectParser.ProjectParser('../../project.xml')
    buildSod.build(proj)
    buildScripts(proj)
    depCopy.copy(proj)
    os.chdir(startdir)
    print 'started ' + command
    if command.endswith('.bat'):
        os.spawnlp(os.P_WAIT, command, 'cmd', command,)
    else:
        os.spawnlp(os.P_WAIT, command, 'sh', command)
    print '\ndone'

if __name__ == "__main__":
    main(sys.argv[1:])
