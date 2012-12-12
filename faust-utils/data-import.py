#!/usr/bin/env python
#

import os
import sys
from time import sleep
from subprocess import check_call, call
from deploy import pids, is_alive, NAME, APP_DIR

IMPORT_DIR = '/mnt/data/import-db'
DATA_DIR = '/mnt/data/db'
KILL_TIMEOUT = 20.0

print 'Cleaning up import dir...'
os.chdir(IMPORT_DIR)
check_call(['rm -rf ' + IMPORT_DIR + '/*'], shell=True)

print 'Importing data'
os.chdir(APP_DIR)
check_call(['java', '-server', '-Xmx512m', '-cp', 'app/lib/' + NAME +'.jar', 'de.faustedition.transcript.TranscriptBatchReader', '/mnt/data/data-import.properties'])

print 'Stopping running application...'
app_pids = pids(NAME)
if len(app_pids) > 0:
	check_call(['kill', app_pids[0]])

for i in range(20):
	sleep(KILL_TIMEOUT / 20.0)
	if not is_alive(NAME):
		break

if is_alive(NAME):
	print 'Process not dead, giving up!'
	sys.exit(1)

print 'Cleaning up data dir...'
os.chdir(DATA_DIR)
check_call(['rm -rf ' + DATA_DIR + '/*'], shell=True)

print 'Copying files...'
os.chdir(IMPORT_DIR)
check_call(['cp -r * ' + DATA_DIR], shell=True)

print 'Restarting app...'
os.chdir(APP_DIR)
check_call(['nohup java -Xmx1024m -server -jar app/lib/' + NAME +'.jar /mnt/data/config.properties &'], shell=True)
print 'All done!'
