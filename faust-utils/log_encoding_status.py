#!/usr/bin/env python
#
# Log the encoding status; to be called on a regular basis
#

import time
import faust
from report_encoding_status import count

filename = faust.config.get("log", "encoded")

status_dict, status_unknown = count()
line = str(time.time()) + "," + str(status_dict["encoded"]) + '\n'

f = open (filename, 'a')
f.write(line)
f.close()


