#! /usr/bin/python -O
import sys, signal, os
from os.path import join
sys.path.append('../../../devTools/maven')
import scriptBuilder, ProjectParser, depCopy
sys.path.append('../../')
import build


def signal_handler(signal, frame):
    sys.exit(0)

def main(argv):
    signal.signal(signal.SIGINT, signal_handler)
    startdir = os.path.abspath('.')
    proj = ProjectParser.ProjectParser('../../project.xml')
    build.buildJars(proj)
    os.chdir(startdir)
    files = filter(lambda x:x.endswith('xsl') or x.endswith('xml'), os.listdir('..'))
    files = ['../' + file for file in files]
    files.append(proj.path + 'target/' + proj.builtJar)
    files.append('buildSchemaDocs.py')
    files.append('elementPage.vm')
    fileModTimes = [os.stat(file).st_mtime for file in files]
    generateTime = os.stat('../generatedSite/tagDocs/sod/start.html').st_mtime
    doTheWork = False
    for modTime in fileModTimes:
        if modTime > generateTime:
            doTheWork = True
    for root, dirs, files in os.walk('../../relax'):
        if not root.count('CVS') > 0:
            for file in files:
                if not file.startswith('.') and os.stat(join(root, file)).st_mtime > generateTime:
                    doTheWork = True
    if doTheWork:
        build.buildSchemaDocScripts(proj)
        depCopy.copy(proj)
        print 'starting schemaDocumenter' 
        if os.environ.has_key('OS') and os.environ['OS'] == 'Windows_NT':
            os.spawnlp(os.P_WAIT, 'schemaDocumenter.bat', 'cmd', 'schemaDocumenter.bat')
        else:
            os.spawnlp(os.P_WAIT, 'schemaDocumenter.sh', 'sh', 'schemaDocumenter.sh')
        print 'done'
    else:
        print "nothing chaged"

if __name__ == "__main__":
    main(sys.argv[1:])
