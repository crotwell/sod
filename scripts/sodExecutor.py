import os, sys, signal
sys.path.append("../../devTools/maven")
import ProjectParser, sodScriptBuilder, mavenExecutor

def signal_handler(signal, frame):
    sys.exit(0)

def main(argv):
    startdir = os.path.abspath('.')
    command = 'sod.bat'
    config = 'revtest.xml'
    if len(argv) > 0: command = argv[0]
    if len(argv) > 1: config = argv[1]
    signal.signal(signal.SIGINT, signal_handler)
    proj = ProjectParser.ProjectParser('../project.xml')
    allProj = [ProjectParser.ProjectParser('../../fissures/project.xml'),
               ProjectParser.ProjectParser('../../fissuresUtil/project.xml'),
               proj]
    for proj in allProj: mavenExecutor.mavenExecutor(proj).jarinst()
    sodScriptBuilder.buildAll(proj)
    os.chdir(startdir)
    print 'started ' + command + ' on ' + config
    if command.endswith('.bat'):
        os.spawnlp(os.P_WAIT, command, 'cmd', command, '-f', config)
    else:
        os.spawnlp(os.P_WAIT, command, 'sh', command, '-f', config)
    print '\ndone'

if __name__ == "__main__":
    main(sys.argv[1:])
