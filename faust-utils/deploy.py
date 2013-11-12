#!/usr/bin/env python
#
# Deploy the application:
# Build, copy, stop the currently running application, make a
# backup, run

import os
import sys
from time import sleep
from subprocess import check_call, call

SRC_DIR = '/mnt/data/src/app/'
APP_DIR = '/mnt/data/'
NAME = 'faust-1.3-SNAPSHOT'
KILL_TIMEOUT = 20.0

def is_alive (name):
        contains = lambda x: x.find(name) >= 0
        logical_or = lambda x, y: x or y
        psout = os.popen('ps xaww')
        return reduce(logical_or, map(contains, psout))

def pids (name):
        return [l.split()[0] for l in os.popen('ps xaww') if name in l]

def pull():
    print 'Pulling source...'
    os.chdir(SRC_DIR)
    check_call(['git', 'pull'])

def build():
    print 'Building application...'
    os.chdir('faust')
    check_call(['mvn','-DskipTests','clean','package'])

def backup():
    print 'Making a backup...'
    os.chdir('/mnt/data')
    call(['rm', '-rf', 'app.bak3'])
    call(['mv', 'app.bak2', 'app.bak3'])
    call(['mv', 'app.bak1', 'app.bak2'])
    call(['mv', 'app.bak', 'app.bak1'])
    check_call(['mv', 'app', 'app.bak'])

def copy():
    print 'Copying new application...'
    os.chdir('target')
    check_call(['cp', NAME + '-app' + '.zip', APP_DIR])

def unzip():
    print 'Unzipping...'
    os.chdir(APP_DIR)
    check_call(['unzip', NAME + '-app' + '.zip'])

def start():
    print 'Restarting app...'
    check_call(['nohup java -Xmx1024m -server -jar app/lib/' + NAME +'.jar /mnt/data/config.properties &'], shell=True)

def stop():
    sys.stdout.write('Stopping running application...')
    app_pids = pids(NAME)
    if len(app_pids) > 0:
            check_call(['kill', app_pids[0]])

    for i in range(30):
            sleep(KILL_TIMEOUT / 30.0)
	    sys.stdout.write('.')
            if not is_alive(NAME):
		    print 'Stopped.'
                    break

    if is_alive(NAME):
            print 'Process not dead, giving up!'
            sys.exit(1)

def read():
    print 'Deleting old database...'
    check_call(['rm -rf db/*'], shell=True)
    print 'Reading database...'
    check_call(['java -Xmx1024m -server -cp app/lib/' + NAME +'.jar de.faustedition.transcript.TranscriptBatchReader /mnt/data/config.properties'], shell=True)
    
def update():

    pull()
    build()
    copy()
    stop()
    backup()
    unzip()
    read()
    start()

    print 'All done!'

if __name__ == "__main__":
    if len(sys.argv) == 2:
        if sys.argv[1] == 'stop':
            stop()
            exit(0)
        elif sys.argv[1] == 'start':
            start()
            exit(0)
        elif sys.argv[1] == 'read':
            stop()
            read()
            start()
            exit(0)
        elif sys.argv[1] == 'update':
            update()
            exit(0)

    print 'usage: deploy.py command'
    print '   commands: stop start read update'
    exit(-1)

