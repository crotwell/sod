#! /usr/bin/python -O
import sys, os
from optparse import OptionParser
sys.path.append('../../devTools/maven')
import scriptBuilder, ProjectParser, depCopy
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

if __name__ == "__main__":
    parser = OptionParser()
    parser.add_option("-c", "--clean", dest="clean",
                      help="remove scripts after running",
                      default=False,
                      action="store_true")
    options = parser.parse_args()[0]
    proj = ProjectParser.ProjectParser('../project.xml')
    scripts = [('buildSchema', 'com.sun.msv.writer.relaxng.Driver sod.rng > ../src/edu/sc/seis/sod/data/sod.rng'),
               ('buildGrouperSchema', 'com.sun.msv.writer.relaxng.Driver network/grouper.rng > ../src/edu/sc/seis/sod/data/grouper.rng'),
               ('serializeGrammar', 'edu.sc.seis.sod.editor.SchemaGrammar'),
               ('validateExamples', 'edu.sc.seis.sod.validator.example.ExampleValidator')]
    for name, mainclass in scripts:
        print 'running ' + name
        if os.environ.has_key('OS') and os.environ['OS'] == 'Windows_NT':
            scriptBuilder.setVarWindows()
            scriptBuilder.build(buildSchemaParams([scriptBuilder.windowsParameters()], proj, name, mainclass), proj, useMavenJars=True)
            os.spawnlp(os.P_WAIT, name + '.bat', 'cmd', name + '.bat')
            if options.clean: os.remove(name + '.bat')
        else:
            scriptBuilder.setVarSh()
            scriptBuilder.build(buildSchemaParams([], proj, name, mainclass), proj, useMavenJars=True)
            os.spawnlp(os.P_WAIT, './' + name + '.sh', 'sh', '')
            if options.clean: os.remove(name + '.sh')
    print """Done. If you're not going to monkey with the schema any more you'll want
to commit elementNode.ser, sod.rng, and grouper.rng in
src/edu/sc/seis/sod/data.  If not, I'll come complain to you.  If I'm
reading this message at a later date I'll want to commit elementNode.ser,
sod.rng and grouper.rng in src/edu/sc/seis/sod/data so that someone else
doesn't come to complain to me."""
