#!/usr/bin/env python
#
# Deploy the application:
# Build, copy, stop the currently running application, make a
# backup, run

import os
import sys
from time import sleep
from subprocess import check_call

SRC_DIR = '/mnt/data/src/trunk/'
APP_DIR = '/mnt/data/'
NAME = 'faust-1.3-SNAPSHOT'
KILL_TIMEOUT = 20

def is_alive (name):
	contains = lambda x: x.find(name) >= 0
	logical_or = lambda x, y: x or y
	psout = os.popen('ps xaww')
	return reduce(logical_or, map(contains, psout))


print 'Pulling source...'
os.chdir(SRC_DIR)
check_call(['bzr', 'pull'])

print 'Building application...'
os.chdir('faust')
check_call(['mvn','clean','package'])

print 'Copying new application...'
os.chdir('target')
check_call(['cp', NAME + '-app' + 'zip', APP_DIR])

print 'Stopping running application...'
check_call(['killall', '-r', NAME + '-app'])
sleep(KILL_TIMEOUT)
if is_alive(NAME):
	print 'Process not dead, giving up!'
	sys.exit(1)

print 'Making a backup...'
call(['rm', '-rf', 'app.bak3'])
call(['mv', 'app.bak2', 'app.bak3'])
call(['mv', 'app.bak1', 'app.bak2'])
call(['mv', 'app.bak', 'app.bak1'])
call_check(['mv', 'app', 'app.bak'])

print 'Unzipping...'
os.chdir(APP_DIR)
check_call(['unzip', NAME + '-app' + '.zip'])

print 'Restarting app...'
check_call(['nohup java -Xmx1024m -server -jar app/lib/' + NAME +'.jar /mnt/data/config.properties &'], shell=True)
print 'All done!'
