#!/usr/bin/env python

import os
import os.path
import sys

def print_stage(stage): print "\n" + (70 * "=") + "\n" + stage + "\n" + (70 * "=") + "\n"

base_dir = os.path.abspath(os.path.dirname(__file__))
args = sys.argv[1:]

# ---------------------------------------- maven targets

mvn_targets = []
for target_candidate in ('clean', 'install', 'deploy'):
	if target_candidate in args: mvn_targets.append(target_candidate)
if len(mvn_targets) == 0: mvn_targets.append('install')
if ('clean' not in mvn_targets): mvn_targets.insert(0, 'source:jar')

# ---------------------------------------- 3rd party modules

if 'deps' in args:
	third_party_dir = os.path.join(base_dir, 'third-party')
	third_party_mvn_opts = []
	if 'deploy' in mvn_targets:
		third_party_mvn_opts.append('-DaltDeploymentRepository=' +\
				'faust-distribution::default::scpexe://faustedition.net/data/faust/maven-repository')

	for third_party_module in os.listdir(third_party_dir):
		if third_party_module.startswith('.'): continue
		os.chdir(os.path.join(third_party_dir, third_party_module))
		print_stage("Building 3rd party module '%s'" % third_party_module)
		os.system(' '.join(['mvn'] + third_party_mvn_opts + mvn_targets))

# ---------------------------------------- Faust modules

print_stage("Building Faust modules")
os.chdir(os.path.join(base_dir, 'project-pom'))
os.system(' '.join(['mvn'] + mvn_targets))

