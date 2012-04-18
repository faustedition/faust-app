#!/usr/bin/env python
#
# Generate pyramidial tile files (ptif) for IIP image server
#

import os
import os.path
import subprocess
import stat
import sys

import faust

convert_cmd = faust.config.get("facsimile", "convert")
tile_dir = "/".join((faust.config.get("facsimile", "dir"), "ptif"))
facs_dir = "/".join((faust.config.get("facsimile", "dir"), "tif"))

# generate missing pyramidial tile files
for root, dirs, files in os.walk(facs_dir):
	for f in files:
		if not f.endswith(".tif"): continue		
		tif_path = "/".join((root, f))
		ptif_path = tile_dir + tif_path[len(facs_dir):]
		
		if os.access(ptif_path, os.F_OK) and\
			os.stat(ptif_path)[stat.ST_MTIME] >= os.stat(tif_path)[stat.ST_MTIME]: continue

		print tif_path, "===>", ptif_path
		
		ptif_dir_path = os.path.dirname(ptif_path)
		if not os.access(ptif_dir_path, os.F_OK): os.makedirs(ptif_dir_path)
		
		subprocess.call([
			convert_cmd,
			tif_path,
			"-quiet",
			"-define", 
			"tiff:tile-geometry=256x256", 
			"-compress", "jpeg",
			"-depth", "8", 
			"ptif:" + ptif_path
		])

# delete non-matching ptif files
for root, dirs, files in os.walk(tile_dir):
	for f in files:
		if not f.endswith(".tif"): continue
		ptif_path = "/".join((root, f))
		tif_path = facs_dir + ptif_path[len(tile_dir):]		
		if not os.access(tif_path, os.F_OK): os.unlink(ptif_path)