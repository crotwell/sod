#! /usr/bin/python -O
import sys, os
from optparse import OptionParser
sys.path.append('../../devTools/maven')
import scriptBuilder, ProjectParser, depCopy, utils
sys.path.append('..')
import build
class buildSchemaParams(scriptBuilder.scriptParameters):
    homeloc = '.'
    def __init__(self, mods, proj, name, mainclass):
        scriptBuilder.scriptParameters.__init__(self)
        for mod in mods: self.update(mod)
        self.name = name
        homevar = self.add('SOD_HOME', self.homeloc, 'initial', 1, True)
        libvar = self.getVar('LIB', 'initial')
        if os.environ.has_key('OS') and os.environ['OS'] == 'Windows_NT':
            stdin, stdout = os.popen2('cygpath -w ' + proj.repo)
            libvar.setValue(stdout.read())
        else:
            libvar.setValue(proj.repo)
        self.mainclass = mainclass
        self.xOptions['mx']='mx256m'
        
def run(name, mainclass, clean):
    print 'running ' + name
    if os.environ.has_key('OS') and os.environ['OS'] == 'Windows_NT':
        scriptBuilder.setVarWindows()
        scriptBuilder.build(buildSchemaParams([scriptBuilder.windowsParameters()], proj, name, mainclass), proj, useMavenJars=True)
        os.spawnlp(os.P_WAIT, name + '.bat', 'cmd', name + '.bat')
        if clean: os.remove(name + '.bat')
    else:
        scriptBuilder.setVarSh()
        scriptBuilder.build(buildSchemaParams([], proj, name, mainclass), proj, useMavenJars=True)
        os.spawnlp(os.P_WAIT, './' + name + '.sh', 'sh', '')
        if clean: os.remove(name + '.sh')

if __name__ == "__main__":
    parser = OptionParser()
    parser.add_option("-f", "--filthy", dest="filthy",
                      help="leave scripts after running",
                      default=False,
                      action="store_true")
    parser.add_option("-v", "--validate", dest="validate",
                      help="validate examples",
                      default=False,
                      action="store_true")
    parser.add_option("-b", "--build", dest="build",
                      help="build jar after the schemas are built but before the examples are validated.  The jars will only be built if the big schema is older than the rng files",
                      default=False,
                      action="store_true")
    options = parser.parse_args()[0]
    proj = ProjectParser.ProjectParser('../project.xml')
    scripts = [('buildSchema', 'com.sun.msv.writer.relaxng.Driver sod.rng > ../src/edu/sc/seis/sod/data/sod.rng'),
               ('buildGrouperSchema', 'com.sun.msv.writer.relaxng.Driver network/grouper.rng > ../src/edu/sc/seis/sod/data/grouper.rng'),
               ('serializeGrammar', 'edu.sc.seis.sod.editor.SchemaGrammar')]
    bigRngModtime = os.stat('../src/edu/sc/seis/sod/data/sod.rng').st_mtime
    buildschema = utils.filesModifiedAfter(bigRngModtime, '.')
    if buildschema:
        for name, mainclass in scripts: run(name, mainclass, not options.filthy)
    else:
        print 'No changes to relax files!'
    if options.build: build.buildJars(proj)
    if options.validate:
        run('validateExamples', 
            'edu.sc.seis.sod.validator.example.ExampleValidator',
            not options.filthy)
    
    if buildschema:    
        print """Done. If you're not going to monkey with the schema any more you'll want
to commit elementNode.ser, sod.rng, and grouper.rng in
src/edu/sc/seis/sod/data.  If not, I'll come complain to you.  If I'm
reading this message at a later date I'll want to commit elementNode.ser,
sod.rng and grouper.rng in src/edu/sc/seis/sod/data so that someone else
doesn't come to complain to me."""
