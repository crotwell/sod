#! /usr/bin/python -O
import os, sys, signal
sys.path.append("../../devTools/maven")
import ProjectParser, buildSodScripts, depCopy
sys.path.append('../')
import buildSod
from optparse import OptionParser

def signal_handler(signal, frame):
    sys.exit(0)

def main(options):
    signal.signal(signal.SIGINT, signal_handler)
    startdir = os.path.abspath('.')
    command = options.command
    config = options.config
    proj = ProjectParser.ProjectParser(options.project)
    buildSod.build(proj)
    print 'built %s scripts' % len(buildSodScripts.buildAll(proj))
    depCopy.copy(proj)
    os.chdir(startdir)
    print 'started ' + command + ' on ' + config
    if command.endswith('.bat'):
        os.spawnlp(os.P_WAIT, command, 'cmd', command, '-f', config, '-props', 'cwg.prop')
    else:
        os.spawnlp(os.P_WAIT, command, 'sh', command, '-f', config)
    print '\ndone'

if __name__ == "__main__":
    parser = OptionParser()
    parser.add_option("-c", "--command", dest="command",
                      help="run COMMAND command", metavar="COMMAND",
                      default="sod.bat")
    parser.add_option("-f", "--config", dest="config",
                      help="use CONFIG as config file", metavar="CONFIG",
                      default="tutorial.xml")
    parser.add_option("-p", "--project", dest="project",
                      help="use PROJECT as the project file", metavar="PROJECT",
                      default="../project.xml")
    main(parser.parse_args()[0])
