#!/usr/bin/env python
#
# Log the encoding status; to be called on a regular basis
#

import time
import faust
from report_encoding_status import count

status_dict, status_unknown = count()
for (key, filename) in faust.config.items("log-status"):
    line = str(time.time()) + "," + str(status_dict[key]) + '\n'
    try:
        f = open (filename, 'a')
        f.write(line)
    finally:
        f.close()



