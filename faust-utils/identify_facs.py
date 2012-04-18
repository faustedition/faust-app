#!/usr/bin/env python
#
# Write out basic metadata of facsimiles
#

import os
import subprocess
import sys

import faust

identify_cmd = [
	faust.config.get("facsimile", "identify"),
	"-quiet",
	"-format", 
	"%[width],%[height],%[xresolution],%[yresolution],%[depth],%[colorspace]"
	]

for root, dirs, files in os.walk("/".join((faust.config.get("facsimile", "dir"), "tif"))):
	for f in files:
		if not f.endswith(".tif"): continue
		path = "/".join((root, f))
		sys.stdout.write(path + ": ")
		sys.stdout.flush()
		subprocess.call(identify_cmd + [ path ])