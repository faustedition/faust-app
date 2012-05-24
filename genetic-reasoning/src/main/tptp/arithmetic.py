#!/usr/bin/python

import subprocess
import sys
import tempfile
import os

axiomnr = 1

def wrap_tptp(type, term):
	global axiomnr
	name = type + str(axiomnr)
	axiomnr = axiomnr + 1
	return 'fof(' + name + ', ' + type +  ', ( ' 	+ term	+ ' )).\n'

def axiom (term):
	return wrap_tptp ('axiom', term)

def question(term):
	return wrap_tptp ('question', term)

def generate_line_axioms (start, end):
	# the previous line is always before
	write (axiom('prev(LX, LY) => before(LX, LY)'))
	# transitivity of before
	write (axiom('(before(LX, LY) & before(LY, LZ)) => before(LX, LZ)'))

	for line in range(start, end):
		thisline = 'line' + str(line)
		nextline = 'line' + str(line + 1)
		# previous line
		write(axiom ('prev( ' + thisline + ', ' + nextline  + ' )'))
		# inequality
		write(axiom (thisline + ' != ' + nextline))
		
def write(s):
	global tmpfile
	tmpfile.write(s)

tmpfile = tempfile.NamedTemporaryFile()
generate_line_axioms(1,7)
write(question('? [X,Y]: before(X,Y)'))
tmpfile.flush()
os.fsync(tmpfile)

subprocess.call('cat ' + tmpfile.name, shell=True)

callstr = "eprover --memory-limit=1024 -xAuto -tAuto --tptp3-format --answers=999 " + tmpfile.name + "| grep 'SZS answers Tuple'"
subprocess.call(callstr, shell=True)

tmpfile.close()

print 'done.'



