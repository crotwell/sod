#! /usr/bin/python -O
import sys, signal, os
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
    build.buildSchemaDocScripts(proj)
    depCopy.copy(proj)
    os.chdir(startdir)
    print 'starting schemaDocumenter' 
    if os.environ.has_key('OS') and os.environ['OS'] == 'Windows_NT':
        os.spawnlp(os.P_WAIT, 'schemaDocumenter.bat', 'cmd', 'schemaDocumenter.bat')
    else:
        os.spawnlp(os.P_WAIT, 'schemaDocumenter.sh', 'sh', 'schemaDocumenter.sh')
    print '\ndone'

if __name__ == "__main__":
    main(sys.argv[1:])
