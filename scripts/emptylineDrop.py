import sys
import re
blanks = re.compile('^\s*$')

for line in sys.stdin:
    if not blanks.match(line):
        print line,
