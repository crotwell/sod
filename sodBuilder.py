import sys, os
sys.path.append("../devTools/maven")
import ProjectParser, mavenExecutor

def build(sodProj):
    curdir = os.path.abspath('.')
    os.chdir(sodProj.path)
    allProj = [ProjectParser.ProjectParser('../fissures/project.xml'),
               ProjectParser.ProjectParser('../fissuresUtil/project.xml'),
               sodProj]
    for proj in allProj: mavenExecutor.mavenExecutor(proj).jarinst()
    os.chdir(curdir)

if __name__ == "__main__":
    build(ProjectParser.ProjectParser())