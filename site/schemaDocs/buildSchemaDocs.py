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
    command = 'schemaDocumenter.bat'
    proj = ProjectParser.ProjectParser('../../project.xml')
    build.buildJars(proj)
    build.buildSchemaDocScripts(proj)
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
