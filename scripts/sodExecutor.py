import os, sys, signal
sys.path.append("../../devTools/maven")
import ProjectParser, sodScriptBuilder, depCopy
sys.path.append('../')
import sodBuilder

def signal_handler(signal, frame):
    sys.exit(0)

def main(argv):
    signal.signal(signal.SIGINT, signal_handler)
    startdir = os.path.abspath('.')
    command = 'sod.bat'
    config = 'revtest.xml'
    proj = ProjectParser.ProjectParser('../project.xml')
    sodBuilder.build(proj)
    if len(argv) > 0: command = argv[0]
    if len(argv) > 1: config = argv[1]
    print 'built %s scripts' % len(sodScriptBuilder.buildAll(proj))
    depCopy.copy(proj)
    os.chdir(startdir)
    print 'started ' + command + ' on ' + config
    if command.endswith('.bat'):
        os.spawnlp(os.P_WAIT, command, 'cmd', command, '-f', config, '-props', 'cwg.prop')
    else:
        os.spawnlp(os.P_WAIT, command, 'sh', command, '-f', config)
    print '\ndone'

if __name__ == "__main__":
    main(sys.argv[1:])
