#! /usr/bin/python -O
import sys
sys.path.append('../../devTools/maven')
import scriptBuilder, ProjectParser, depCopy
class schemaBuilderScriptParams(scriptBuilder.jacorbParameters):
    homeloc = '.'
    def __init__(self, mods):
        scriptBuilder.jacorbParameters.__init__(self)
        for mod in mods: self.update(mod)
        self.name = 'bigSchemaBuilder'
        homevar = self.add('SOD_HOME', self.homeloc, 'initial', 1, True)
        libvar = self.getVar('LIB', 'initial')
        libvar.setValue(homevar.interp+'/lib')
        self.mainclass = ['com.sun.msv.writer.relaxng.Driver sod.rng > ../src/edu/sc/seis/sod/data/sod.rng',
                          'com.sun.msv.writer.relaxng.Driver network/grouper.rng > ../src/edu/sc/seis/sod/data/grouper.rng',
                          'edu.sc.seis.sod.editor.SchemaGrammar',
                          'edu.sc.seis.sod.validator.example.ExampleValidator']
        self.xOptions['mx']='mx256m'


def build(proj):
    scriptBuilder.setVarSh()
    scripts = [scriptBuilder.build(schemaBuilderScriptParams([]), proj)]
    scriptBuilder.setVarWindows()
    scripts.append(scriptBuilder.build(schemaBuilderScriptParams([scriptBuilder.windowsParameters()]), proj))
    return scripts

if __name__ == "__main__":
    proj = ProjectParser.ProjectParser('../project.xml')
    depCopy.copy(proj)
    build(proj)
