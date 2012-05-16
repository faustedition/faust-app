#!/usr/bin/python

import subprocess

callstr = "eprover -xAuto -tAuto --tptp3-format --answers=999 axioms.p | grep 'SZS answers Tuple'"

subprocess.call(callstr, shell=True)
